// js/cursos.js

document.addEventListener('DOMContentLoaded', () => {

    // URL base de tu API REST para cursos
    const API_CURSOS_URL = 'http://localhost:8080/api/cursos';
    const API_PROFESORES_URL = 'http://localhost:8080/api/profesores';

    // Referencias a elementos clave del DOM
    const formCurso = document.getElementById('formCurso');
    const cuerpoTablaCursos = document.getElementById('cuerpoTablaCursos');
    const profesoresSelect = document.getElementById('profesoresSelect'); // Para el select de profesores
    const messageDiv = document.getElementById('message'); // Asumiendo que tienes un div con id="message" para mensajes de estado

    // Campo oculto para guardar el ID del curso a editar (asegúrate de que este ID esté en tu HTML o se cree dinámicamente)
    let cursoIdInput = document.getElementById('cursoId');
    if (!cursoIdInput) { // Si no existe en el HTML, lo creamos
        cursoIdInput = document.createElement('input');
        cursoIdInput.type = 'hidden';
        cursoIdInput.id = 'cursoId';
        cursoIdInput.name = 'id';
        formCurso.appendChild(cursoIdInput);
    }

    let modoEdicion = false; // Variable para controlar si estamos creando o editando

    // Cache para almacenar nombres de profesores por ID y evitar múltiples llamadas
    const profesoresCache = {};

    // Función para mostrar mensajes de estado en el UI
    function showMessage(msg, type = 'success') {
        if (messageDiv) {
            messageDiv.textContent = msg;
            messageDiv.className = `alert alert-${type} mt-3`;
            messageDiv.classList.remove('d-none');
            // Ocultar el mensaje después de 5 segundos
            setTimeout(() => {
                messageDiv.classList.add('d-none');
            }, 5000);
        } else {
            console.log(`Mensaje (${type}): ${msg}`); // Fallback si no hay messageDiv
            alert(msg); // Para asegurar que el usuario vea el mensaje
        }
    }

    // --- FUNCIÓN CLAVE: Obtener Encabezados de Autorización con JWT ---
    function getAuthHeaders() {
        const token = localStorage.getItem('jwtToken');
        if (token) {
            return {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}` // Formato estándar para JWT
            };
        } else {
            console.warn('No se encontró el token JWT. Redirigiendo al login.');
            showMessage('Tu sesión ha expirado o no has iniciado sesión. Redirigiendo...', 'warning');
            localStorage.removeItem('jwtToken'); // Limpiar token
            localStorage.removeItem('userRoles'); // Limpiar roles también
            setTimeout(() => {
                window.location.href = '/login.html'; // Redirigir al login después de un breve retraso
            }, 1000);
            return {}; // Retornar objeto vacío para que las llamadas fetch no se intenten sin token
        }
    }

    // --- NUEVA FUNCIÓN: Verificar roles de usuario ---
    // Asume que los roles se guardan en localStorage como un JSON string de un array (ej: "['ROLE_USER', 'ROLE_ADMIN']")
    function hasRole(role) {
        const userRolesString = localStorage.getItem('userRoles');
        if (!userRolesString) {
            return false;
        }
        try {
            const userRoles = JSON.parse(userRolesString);
            return Array.isArray(userRoles) && userRoles.includes(role);
        } catch (e) {
            console.error("Error al parsear roles de usuario desde localStorage:", e);
            return false;
        }
    }

    // Función para obtener el nombre de un profesor dado su ID (o de la caché)
    async function getNombreProfesor(profesorId) {
        if (profesoresCache[profesorId]) {
            return profesoresCache[profesorId];
        }
        try {
            const headers = getAuthHeaders(); // Obtener headers con JWT
            if (!headers['Authorization']) return `ID: ${profesorId} (No Auth)`; // Si no hay token, no intentar fetch

            const response = await fetch(`${API_PROFESORES_URL}/${profesorId}`, {
                headers: headers // Pasar headers a la solicitud
            });
            if (!response.ok) {
                console.warn(`No se pudo obtener el nombre del profesor con ID ${profesorId}. Status: ${response.status}`);
                return `ID: ${profesorId}`; // Retornar el ID si no se puede obtener el nombre
            }
            const profesor = await response.json();
            const fullName = `${profesor.nombre} ${profesor.apellidos}`; // Asumiendo que el campo es 'apellidos'
            profesoresCache[profesorId] = fullName; // Guardar en caché
            return fullName;
        } catch (error) {
            console.error(`Error al obtener nombre de profesor ${profesorId}:`, error);
            return `ID: ${profesorId}`; // En caso de error, retornar el ID
        }
    }

    // Función para cargar los profesores disponibles en el select
    async function cargarProfesoresEnSelect() {
        try {
            const headers = getAuthHeaders(); // Obtener headers con JWT
            if (!headers['Authorization']) return;

            const response = await fetch(API_PROFESORES_URL, {
                headers: headers // Pasar headers a la solicitud
            });
            if (!response.ok) {
                // Si la respuesta no es OK, leer el texto para ver si hay un mensaje de error HTTP
                const errorBody = await response.text(); // Lee como texto primero
                let errorMessage = `Error HTTP al cargar profesores: ${response.status} - ${response.statusText}`;

                try {
                    const errorJson = JSON.parse(errorBody); // Intenta parsear a JSON
                    errorMessage = errorJson.message || JSON.stringify(errorJson);
                } catch (e) {
                    // Si no es un JSON válido, usa el texto crudo o el mensaje HTTP
                    errorMessage += `. Detalle: ${errorBody.substring(0, 200)}...`; // Limita el detalle
                }

                throw new Error(errorMessage);
            }
            const profesores = await response.json();
            console.log("Profesores disponibles para selección:", profesores);

            profesoresSelect.innerHTML = '<option value="" disabled selected>Seleccione un profesor...</option>';

            if (profesores && profesores.length > 0) {
                profesores.forEach(profesor => {
                    profesoresCache[profesor.id] = `${profesor.nombre} ${profesor.apellidos}`; // Guardar en caché
                    const option = document.createElement('option');
                    option.value = profesor.id;
                    option.textContent = `${profesor.nombre} ${profesor.apellidos}`;
                    profesoresSelect.appendChild(option);
                });
            } else {
                const option = document.createElement('option');
                option.value = '';
                option.textContent = 'No hay profesores disponibles';
                option.disabled = true;
                profesoresSelect.appendChild(option);
            }
        } catch (error) {
            console.error("Error al cargar profesores para el select:", error);
            // MODIFICACIÓN CLAVE AQUÍ: Solo mostrar la alerta si el usuario es ADMIN
            if (hasRole('ROLE_ADMIN')) {
                showMessage("Error al cargar profesores disponibles: " + error.message, 'danger');
            } else {
                // Para usuarios no administradores, este error es esperado y no es crítico para su funcionalidad.
                // Simplemente lo registramos en consola, no mostramos una alerta al usuario.
                console.warn("Intento de cargar profesores como no-admin. Es un comportamiento esperado si el endpoint está protegido.");
            }
        }
    }

    // Función para cargar y mostrar cursos en la tabla (GET /api/cursos)
    async function cargarCursos() {
        console.log("Iniciando carga de cursos...");
        try {
            const headers = getAuthHeaders(); // Obtener headers con JWT
            if (!headers['Authorization']) return;

            const response = await fetch(API_CURSOS_URL, {
                headers: headers // Pasar headers a la solicitud
            });

            if (!response.ok) {
                const errorData = await response.json(); // Intentar leer detalle del error
                throw new Error(`Error HTTP: ${response.status} - ${errorData.message || response.statusText}`);
            }

            const data = await response.json();
            console.log("Cursos cargados exitosamente:", data);

            cuerpoTablaCursos.innerHTML = ''; // Limpiar la tabla

            // Determinar si el usuario actual es ADMINISTRADOR
            const isAdmin = hasRole('ROLE_ADMIN');

            if (data && data.length > 0) {
                const cursosConNombresProfesores = await Promise.all(data.map(async curso => {
                    const profesoresNombres = [];
                    if (curso.profesoresIds && curso.profesoresIds.length > 0) {
                        const primerProfesorId = curso.profesoresIds[0]; // Tomar el primer profesor del array de IDs
                        if (primerProfesorId) {
                            profesoresNombres.push(await getNombreProfesor(primerProfesorId));
                        }
                    }
                    return { ...curso,
                        profesoresNombres: profesoresNombres.join(', ')
                    };
                }));

                cursosConNombresProfesores.forEach(curso => {
                    const fila = document.createElement('tr');

                    fila.insertCell(0).textContent = curso.id;
                    fila.insertCell(1).textContent = curso.nombre;
                    fila.insertCell(2).textContent = curso.descripcion;
                    fila.insertCell(3).textContent = curso.modalidad;
                    fila.insertCell(4).textContent = curso.profesoresNombres || 'Ninguno';

                    const accionesCelda = document.createElement('td');

                    // --- Control de visibilidad de botones "Editar" y "Eliminar" ---
                    if (isAdmin) {
                        const btnEditar = document.createElement('button');
                        btnEditar.textContent = 'Editar';
                        btnEditar.classList.add('btn', 'btn-info', 'btn-sm', 'me-1', 'btn-editar');
                        btnEditar.dataset.id = curso.id;
                        accionesCelda.appendChild(btnEditar);

                        const btnEliminar = document.createElement('button');
                        btnEliminar.textContent = 'Eliminar';
                        btnEliminar.classList.add('btn', 'btn-danger', 'btn-sm', 'btn-eliminar');
                        btnEliminar.dataset.id = curso.id;
                        accionesCelda.appendChild(btnEliminar);
                    } else {
                        accionesCelda.textContent = 'N/A'; // O dejar vacío si prefieres
                    }

                    fila.appendChild(accionesCelda);
                    cuerpoTablaCursos.appendChild(fila);
                });
            } else {
                const filaVacia = document.createElement('tr');
                const celdaMensaje = document.createElement('td');
                celdaMensaje.colSpan = 6; // Ajusta el colspan según tus columnas
                celdaMensaje.textContent = 'No hay cursos registrados aún.';
                celdaMensaje.classList.add('text-center', 'text-muted');
                filaVacia.appendChild(celdaMensaje);
                cuerpoTablaCursos.appendChild(filaVacia);
            }
        } catch (error) {
            console.error("Hubo un error al cargar los cursos:", error);
            showMessage(`Error al cargar cursos: ${error.message || 'Verifica la conexión o tus permisos.'}`, 'danger');
        }
    }

    // Función para eliminar un curso (DELETE /api/cursos/{id})
    async function eliminarCurso(id) {
        // Verificar rol antes de intentar eliminar
        if (!hasRole('ROLE_ADMIN')) {
            showMessage('No tienes permisos para eliminar cursos.', 'danger');
            return;
        }

        try {
            const headers = getAuthHeaders();
            if (!headers['Authorization']) return;

            const response = await fetch(`${API_CURSOS_URL}/${id}`, {
                method: 'DELETE',
                headers: headers
            });

            if (!response.ok) {
                const errorText = await response.text(); // Puede que no sea JSON en DELETE
                throw new Error(`Error al eliminar: ${response.status} - ${response.statusText}. Detalle: ${errorText}`);
            }

            console.log(`Curso con ID ${id} eliminado exitosamente.`);
            showMessage(`Curso con ID ${id} eliminado exitosamente.`, 'success');

            cargarCursos(); // Recargar la tabla

        } catch (error) {
            console.error("Hubo un error al eliminar el curso:", error);
            showMessage(`Error al eliminar curso: ${error.message}`, 'danger');
        }
    }

    // Función para cargar los datos de un curso en el formulario para edición (GET /api/cursos/{id})
    async function cargarCursoEnFormulario(id) {
        // Verificar rol antes de intentar editar
        if (!hasRole('ROLE_ADMIN')) {
            showMessage('No tienes permisos para editar cursos.', 'danger');
            return;
        }

        try {
            const headers = getAuthHeaders();
            if (!headers['Authorization']) return;

            const response = await fetch(`${API_CURSOS_URL}/${id}`, {
                headers: headers
            });
            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(`Error al cargar curso para editar: ${response.status} - ${errorData.message || response.statusText}`);
            }
            const curso = await response.json();
            console.log("Curso cargado para edición:", curso);

            document.getElementById('nombre').value = curso.nombre;
            document.getElementById('descripcion').value = curso.descripcion;
            document.getElementById('modalidad').value = curso.modalidad;
            cursoIdInput.value = curso.id;

            for (let i = 0; i < profesoresSelect.options.length; i++) {
                profesoresSelect.options[i].selected = false;
            }

            if (curso.profesoresIds && curso.profesoresIds.length > 0) {
                const profesorIdAsignado = curso.profesoresIds[0];
                for (let i = 0; i < profesoresSelect.options.length; i++) {
                    const option = profesoresSelect.options[i];
                    if (parseInt(option.value) === profesorIdAsignado) {
                        option.selected = true;
                        break;
                    }
                }
            }

            modoEdicion = true;
            const submitButton = formCurso.querySelector('button[type="submit"]');
            if (submitButton) {
                submitButton.textContent = 'Actualizar Curso';
            }
            formCurso.scrollIntoView({ behavior: 'smooth' });

            const formTabButton = document.getElementById('form-tab');
            // Usar 'bootstrap' directamente para el control de pestañas si está disponible
            if (formTabButton && typeof bootstrap !== 'undefined' && bootstrap.Tab) {
                const bsTab = new bootstrap.Tab(formTabButton);
                bsTab.show();
            }

        } catch (error) {
            console.error("Error cargando curso para edición:", error);
            showMessage("Error al cargar curso para edición: " + error.message, 'danger');
        }
    }

    // Evento para el envío del formulario (Crear o Actualizar)
    if (formCurso) {
        formCurso.addEventListener('submit', async (event) => {
            event.preventDefault();

            // Verificar rol antes de guardar/actualizar
            if (!hasRole('ROLE_ADMIN')) {
                showMessage('No tienes permisos para guardar o actualizar cursos.', 'danger');
                return;
            }

            const id = cursoIdInput.value;
            const nombre = document.getElementById('nombre').value;
            const descripcion = document.getElementById('descripcion').value;
            const modalidad = document.getElementById('modalidad').value;
            const profesorSeleccionadoId = profesoresSelect.value;
            const profesoresIds = [];
            if (profesorSeleccionadoId) {
                profesoresIds.push(parseInt(profesorSeleccionadoId));
            }

            const cursoData = {
                id: id || null,
                nombre: nombre,
                descripcion: descripcion,
                modalidad: modalidad,
                profesoresIds: profesoresIds
            };

            let method = 'POST';
            let url = API_CURSOS_URL;
            let successMessage = "Curso guardado exitosamente.";

            if (modoEdicion) {
                method = 'PUT';
                url = `${API_CURSOS_URL}/${id}`;
                successMessage = "Curso actualizado exitosamente.";
                console.log("Actualizando curso con ID:", id, "Datos:", cursoData);
            } else {
                console.log("Creando nuevo curso. Datos:", cursoData);
            }

            try {
                const headers = getAuthHeaders();
                if (!headers['Authorization']) return;

                const response = await fetch(url, {
                    method: method,
                    headers: headers,
                    body: JSON.stringify(cursoData)
                });

                if (!response.ok) {
                    let errorData;
                    try {
                        errorData = await response.json();
                    } catch (e) {
                        throw new Error(`Error HTTP: ${response.status} - ${response.statusText}`);
                    }
                    throw new Error(errorData.message || JSON.stringify(errorData) || 'Error al procesar el curso');
                }

                const data = await response.json();
                console.log(successMessage, data);
                showMessage(successMessage + " ID: " + data.id, 'success');

                formCurso.reset();
                cursoIdInput.value = '';
                modoEdicion = false;
                const submitButton = formCurso.querySelector('button[type="submit"]');
                if (submitButton) {
                    submitButton.textContent = 'Guardar Curso';
                }
                profesoresSelect.value = '';

                cargarCursos(); // Recargar la lista de cursos

                const listTabButton = document.getElementById('list-tab');
                if (listTabButton && typeof bootstrap !== 'undefined' && bootstrap.Tab) {
                    const bsTab = new bootstrap.Tab(listTabButton);
                    bsTab.show();
                }

            } catch (error) {
                console.error("Hubo un error al procesar el curso:", error);
                let errorMessage = "Error desconocido al procesar curso.";
                try {
                    const parsedError = JSON.parse(error.message);
                    if (typeof parsedError === 'object' && parsedError !== null && parsedError.message) {
                        errorMessage = parsedError.message;
                    } else if (typeof parsedError === 'object' && parsedError !== null) {
                        errorMessage = Object.values(parsedError).join(', ');
                    } else {
                        errorMessage = error.message;
                    }
                } catch (e) {
                    errorMessage = error.message;
                }
                showMessage("Error: " + errorMessage, 'danger');
            }
        });
    }

    // Botón para limpiar el formulario
    const btnLimpiarUserForm = document.getElementById('btnLimpiarUserForm');
    if (btnLimpiarUserForm) {
        btnLimpiarUserForm.addEventListener('click', limpiarFormulario);
    }
    // (Asegúrate de que la función limpiarFormulario está definida o si no, la función formCurso.reset() es suficiente)


    // Delegación de Eventos para botones "Editar" y "Eliminar" (generados dinámicamente)
    document.addEventListener('click', async (event) => {
        if (event.target.classList.contains('btn-eliminar')) {
            const confirmar = confirm('¿Estás seguro de que deseas eliminar este curso?');
            if (confirmar) {
                const cursoId = event.target.dataset.id;
                await eliminarCurso(cursoId);
            }
        } else if (event.target.classList.contains('btn-editar')) {
            const cursoId = event.target.dataset.id;
            console.log('Intentando cargar curso para edición con ID:', cursoId);
            await cargarCursoEnFormulario(cursoId);
        }
    });

    // --- Carga inicial al cargar la página ---
    // Estas funciones se llaman aquí para que la tabla y el select se llenen automáticamente
    // MODIFICACIÓN CLAVE AQUÍ: Cargar profesores solo si el usuario tiene el rol ADMIN
    if (hasRole('ROLE_ADMIN')) {
        cargarProfesoresEnSelect();
    }
    cargarCursos(); // Cargar cursos siempre para todos los usuarios

    // --- Lógica para ocultar/mostrar elementos de navegación (ejemplo) ---
    // ESTO DEBE ESTAR EN UN ARCHIVO JS GLOBAL (ej. main.js) O EN CADA PÁGINA
    // donde aparezcan los enlaces de navegación, Y DEBERÁS AÑADIR IDs a esos enlaces.
    function setupNavigationVisibility() {
        // Asume que tus <li> o <a> de navegación tienen IDs específicos, por ejemplo:
        const navEstudiantesLink = document.getElementById('navEstudiantes');
        const navProfesoresLink = document.getElementById('navProfesores');
        const isAdmin = hasRole('ROLE_ADMIN');

        console.log("DEBUG: setupNavigationVisibility ejecutada en cursos.js");
        console.log("DEBUG: ¿Es Admin?", isAdmin);

        if (navEstudiantesLink) {
            navEstudiantesLink.style.display = isAdmin ? '' : 'none'; // Mostrar si es admin, ocultar si no
        }
        if (navProfesoresLink) {
            navProfesoresLink.style.display = isAdmin ? '' : 'none'; // Mostrar si es admin, ocultar si no
        }

        // --- Lógica para la pestaña "Registrar/Editar Curso" específica de cursos.html ---
        // Asume que la pestaña "Registrar/Editar Curso" tiene el ID 'registrarCursoTabItem'
        const registrarCursoTabItem = document.getElementById('registrarCursoTabItem');
        console.log("DEBUG: Elemento 'registrarCursoTabItem' encontrado:", registrarCursoTabItem);

        if (registrarCursoTabItem) {
            registrarCursoTabItem.style.display = isAdmin ? '' : 'none';
            // Si el usuario no es admin y la pestaña de registrar curso es la activa,
            // cambiar a la pestaña de lista para evitar que se quede en un tab oculto.
            if (!isAdmin && registrarCursoTabItem.classList.contains('active')) {
                const listTabButton = document.getElementById('list-tab');
                if (listTabButton && typeof bootstrap !== 'undefined' && bootstrap.Tab) {
                    const bsTab = new bootstrap.Tab(listTabButton);
                    bsTab.show();
                    console.log("DEBUG: Cambiando a la pestaña 'Listado de Cursos' porque 'Registrar/Editar Curso' está oculta.");
                }
            }
        } else {
            console.warn("DEBUG: ¡Advertencia! No se encontró el elemento con ID 'registrarCursoTabItem'. Asegúrate de que tu HTML tiene <li id=\"registrarCursoTabItem\" role=\"presentation\"> o similar para la pestaña de registro/edición de cursos.");
        }
    }

    // Llamar a la función de configuración de navegación al cargar la página
    setupNavigationVisibility();

}); // Fin de DOMContentLoaded