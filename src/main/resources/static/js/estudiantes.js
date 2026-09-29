// js/estudiantes.js

document.addEventListener('DOMContentLoaded', () => {

    const API_ESTUDIANTES_URL = 'http://localhost:8080/api/estudiantes';
    const API_CURSOS_URL = 'http://localhost:8080/api/cursos';

    const btnCargarEstudiantes = document.getElementById('btnCargarEstudiantes');
    const formEstudiante = document.getElementById('formEstudiante');
    const cuerpoTablaEstudiantes = document.getElementById('cuerpoTablaEstudiantes');

    const estudianteIdInput = document.getElementById('estudianteId');

    const primerNombreInput = document.getElementById('primerNombre');
    const segundoNombreInput = document.getElementById('segundoNombre');
    const apellidosInput = document.getElementById('apellidos');
    const emailInput = document.getElementById('email');
    const fechaNacimientoInput = document.getElementById('fechaNacimiento');
    const nacionalidadInput = document.getElementById('nacionalidad');

    const cursosSelect = document.getElementById('cursos');

    let modoEdicion = false;

    const listTabBtn = document.getElementById('list-tab');
    const formTabBtn = document.getElementById('form-tab');
    const listTabContent = new bootstrap.Tab(listTabBtn);
    const formTabContent = new bootstrap.Tab(formTabBtn);

    // NUEVO: Variable para almacenar los cursos mapeados por ID
    let cursosMap = new Map();

    function mostrarAlerta(mensaje, tipo = 'info') {
        console.log(`Alerta (${tipo}): ${mensaje}`);
        alert(mensaje);
    }

    async function cargarCursosParaSelect() {
        console.log("Cargando cursos para el select...");
        try {
            // *** CORRECCIÓN APLICADA AQUÍ: Añadiendo headers: window.getAuthHeaders() ***
            const response = await fetch(API_CURSOS_URL, {
                headers: window.getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`Error HTTP al cargar cursos: ${response.status} - ${response.statusText}`);
            }
            const cursos = await response.json();
            console.log("Cursos disponibles:", cursos);

            // NUEVO: Rellenar el mapa de cursos
            cursosMap.clear(); // Limpiar antes de rellenar
            if (cursos && cursos.length > 0) {
                cursos.forEach(curso => {
                    cursosMap.set(curso.id, curso);
                });
            }

            const defaultOption = cursosSelect.querySelector('option[value=""][disabled][selected]');
            cursosSelect.innerHTML = '';
            if (defaultOption) {
                cursosSelect.appendChild(defaultOption);
            } else {
                const placeholderOption = document.createElement('option');
                placeholderOption.value = '';
                placeholderOption.textContent = 'Seleccione un curso...';
                placeholderOption.disabled = true;
                placeholderOption.selected = true;
                cursosSelect.appendChild(placeholderOption);
            }

            if (cursos && cursos.length > 0) {
                cursos.forEach(curso => {
                    const option = document.createElement('option');
                    option.value = curso.id;
                    // Asegúrate de que tu CursoDTO en el backend tenga un campo 'profesor' (ProfesorSimplificadoDTO)
                    // o ajusta esta línea si solo envías el ID del profesor.
                    // Si 'profesor' es un objeto anidado, esta parte es correcta.
                    const profesorNombre = curso.profesoresIds && curso.profesoresIds.length > 0
                        ? `ID: ${curso.profesoresIds[0]}` // Solo muestra el primer ID del profesor si CursoDTO tiene profesoresIds
                        : 'N/A';
                    option.textContent = `${curso.nombre} (Profesor: ${profesorNombre})`;
                    cursosSelect.appendChild(option);
                });
            } else {
                const option = document.createElement('option');
                option.value = '';
                option.textContent = 'No hay cursos disponibles';
                option.disabled = true;
                cursosSelect.appendChild(option);
            }
        } catch (error) {
            console.error("Error al cargar cursos para el select:", error);
            mostrarAlerta("Error al cargar la lista de cursos disponibles: " + error.message, 'danger');
        }
    }

    async function cargarEstudiantes() {
        console.log("Iniciando carga de estudiantes...");
        try {
            // MODIFICACIÓN: Añadir las cabeceras de autorización
            const response = await fetch(API_ESTUDIANTES_URL, {
                headers: window.getAuthHeaders() // <-- AÑADIDO
            });

            if (!response.ok) {
                throw new Error(`Error HTTP: ${response.status} - ${response.statusText}`);
            }

            const data = await response.json();
            console.log("Estudiantes cargados exitosamente:", data);

            cuerpoTablaEstudiantes.innerHTML = '';

            if (data && data.length > 0) {
                data.forEach(estudiante => {
                    const fila = document.createElement('tr');

                    const idCelda = document.createElement('td');
                    idCelda.textContent = estudiante.id;
                    fila.appendChild(idCelda);

                    const primerNombreCelda = document.createElement('td');
                    primerNombreCelda.textContent = estudiante.primerNombre;
                    fila.appendChild(primerNombreCelda);

                    const segundoNombreCelda = document.createElement('td');
                    segundoNombreCelda.textContent = estudiante.segundoNombre || '';
                    fila.appendChild(segundoNombreCelda);

                    const apellidosCelda = document.createElement('td');
                    apellidosCelda.textContent = estudiante.apellidos;
                    fila.appendChild(apellidosCelda);

                    const cursosCelda = document.createElement('td');
                    // MODIFICADO: Usar estudiante.cursosIds
                    if (estudiante.cursosIds && estudiante.cursosIds.length > 0) {
                        const nombresCursos = estudiante.cursosIds.map(cursoId => {
                            const cursoEncontrado = cursosMap.get(cursoId);
                            if (cursoEncontrado) {
                                // Aquí puedes mostrar el nombre del profesor si tu CursoDTO lo incluye
                                // O si tienes un mapeo de profesorId a nombre de profesor
                                const profesorInfo = cursoEncontrado.profesoresIds && cursoEncontrado.profesoresIds.length > 0
                                    ? `(Profesor ID: ${cursoEncontrado.profesoresIds[0]})`
                                    : ''; // Ajusta esto si CursoDTO tiene un objeto ProfesorSimplificadoDTO
                                return `${cursoEncontrado.nombre} ${profesorInfo}`;
                            }
                            return `ID desconocido: ${cursoId}`;
                        }).join(', '); // Unir los nombres de los cursos con comas

                        cursosCelda.textContent = nombresCursos;
                    } else {
                        cursosCelda.textContent = 'Ninguno';
                    }
                    fila.appendChild(cursosCelda);

                    const accionesCelda = document.createElement('td');

                    const btnEditar = document.createElement('button');
                    btnEditar.textContent = 'Editar';
                    btnEditar.classList.add('btn', 'btn-info', 'btn-sm', 'me-1', 'btn-editar');
                    btnEditar.dataset.id = estudiante.id;
                    accionesCelda.appendChild(btnEditar);

                    const btnEliminar = document.createElement('button');
                    btnEliminar.textContent = 'Eliminar';
                    btnEliminar.classList.add('btn', 'btn-danger', 'btn-sm', 'btn-eliminar');
                    btnEliminar.dataset.id = estudiante.id;
                    accionesCelda.appendChild(btnEliminar);

                    fila.appendChild(accionesCelda);
                    cuerpoTablaEstudiantes.appendChild(fila);
                });
            } else {
                const filaVacia = document.createElement('tr');
                const celdaMensaje = document.createElement('td');
                celdaMensaje.colSpan = 6;
                celdaMensaje.textContent = 'No hay estudiantes registrados aún.';
                celdaMensaje.classList.add('text-center', 'text-muted');
                filaVacia.appendChild(celdaMensaje);
                cuerpoTablaEstudiantes.appendChild(filaVacia);
            }
        } catch (error) {
            console.error("Hubo un error al cargar los estudiantes:", error);
            mostrarAlerta("Error al cargar estudiantes: " + error.message, 'danger');
        }
    }

    async function eliminarEstudiante(id) {
        try {
            // MODIFICACIÓN: Añadir las cabeceras de autorización
            const response = await fetch(`${API_ESTUDIANTES_URL}/${id}`, {
                method: 'DELETE',
                headers: window.getAuthHeaders() // <-- AÑADIDO
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Error al eliminar: ${response.status} - ${response.statusText}. Detalle: ${errorText}`);
            }

            console.log(`Estudiante con ID ${id} eliminado exitosamente.`);
            mostrarAlerta(`Estudiante con ID ${id} eliminado exitosamente.`, 'success');

            cargarEstudiantes();

        } catch (error) {
            console.error("Hubo un error al eliminar el estudiante:", error);
            mostrarAlerta("Error al eliminar estudiante: " + error.message, 'danger');
        }
    }

    async function cargarEstudianteEnFormulario(id) {
        try {
            // MODIFICACIÓN: Añadir las cabeceras de autorización
            const response = await fetch(`${API_ESTUDIANTES_URL}/${id}`, {
                headers: window.getAuthHeaders() // <-- AÑADIDO
            });
            if (!response.ok) {
                throw new Error(`Error al cargar estudiante para editar: ${response.status} - ${response.statusText}`);
            }
            const estudiante = await response.json();
            console.log("Estudiante cargado para edición:", estudiante);

            primerNombreInput.value = estudiante.primerNombre;
            segundoNombreInput.value = estudiante.segundoNombre || '';
            apellidosInput.value = estudiante.apellidos;
            emailInput.value = estudiante.email;
            fechaNacimientoInput.value = estudiante.fechaNacimiento;
            nacionalidadInput.value = estudiante.nacionalidad;

            // MODIFICADO: Seleccionar el primer curso si existe en cursosIds
            if (estudiante.cursosIds && estudiante.cursosIds.length > 0) {
                cursosSelect.value = estudiante.cursosIds[0];
            } else {
                cursosSelect.value = '';
            }

            estudianteIdInput.value = estudiante.id;
            modoEdicion = true;
            formEstudiante.querySelector('button[type="submit"]').textContent = 'Actualizar Estudiante';
            formEstudiante.scrollIntoView({ behavior: 'smooth' });

            formTabContent.show();

        } catch (error) {
            console.error("Error cargando estudiante para edición:", error);
            mostrarAlerta("Error al cargar estudiante para edición: " + error.message, 'danger');
        }
    }

    function limpiarFormulario() {
        formEstudiante.reset();
        estudianteIdInput.value = '';
        modoEdicion = false;
        formEstudiante.querySelector('button[type="submit"]').textContent = 'Guardar Estudiante';
        cursosSelect.value = '';
    }

    if (btnCargarEstudiantes) {
        btnCargarEstudiantes.addEventListener('click', cargarEstudiantes);
    }

    if (formEstudiante) {
        formEstudiante.addEventListener('submit', async (event) => {
            event.preventDefault();

            const id = estudianteIdInput.value;
            const primerNombre = primerNombreInput.value;
            const segundoNombre = segundoNombreInput.value;
            const apellidos = apellidosInput.value;
            const email = emailInput.value;
            const fechaNacimiento = fechaNacimientoInput.value;
            const nacionalidad = nacionalidadInput.value;

            const selectedCursoId = cursosSelect.value ? parseInt(cursosSelect.value) : null;
            // MODIFICADO: Asegurarse de enviar solo un ID o un array vacío si no hay selección
            const cursosIdsToSend = selectedCursoId !== null ? [selectedCursoId] : [];

            const segundoNombreAEnviar = segundoNombre === '' ? null : segundoNombre;

            const estudianteData = {
                primerNombre: primerNombre,
                segundoNombre: segundoNombreAEnviar,
                apellidos: apellidos,
                email: email,
                fechaNacimiento: fechaNacimiento,
                nacionalidad: nacionalidad,
                cursosIds: cursosIdsToSend // Correcto: enviamos el array de IDs
            };

            let method = 'POST';
            let url = API_ESTUDIANTES_URL;
            let successMessage = "Estudiante guardado exitosamente.";

            if (modoEdicion) {
                method = 'PUT';
                url = `${API_ESTUDIANTES_URL}/${id}`;
                successMessage = "Estudiante actualizado exitosamente.";
                console.log("Actualizando estudiante con ID:", id, "Datos:", estudianteData);
            } else {
                console.log("Creando nuevo estudiante. Datos:", estudianteData);
            }

            try {
                const response = await fetch(url, {
                    method: method,
                    headers: {
                        'Content-Type': 'application/json',
                        ...window.getAuthHeaders() // <-- AÑADIDO
                    },
                    body: JSON.stringify(estudianteData)
                });

                if (!response.ok) {
                    let errorData;
                    try {
                        errorData = await response.json();
                    } catch (e) {
                        const textError = await response.text();
                        throw new Error(`Error HTTP: ${response.status} - ${response.statusText}. Detalle: ${textError}`);
                    }
                    if (errorData && errorData.errors && Array.isArray(errorData.errors) && errorData.errors.length > 0) {
                        const messages = errorData.errors.map(err => err.defaultMessage || err.field + ': ' + err.code);
                        throw new Error("Errores de validación: " + messages.join(', '));
                    }
                    throw new Error(errorData.message || 'Error al procesar el estudiante');
                }

                const data = await response.json();
                console.log(successMessage, data);
                mostrarAlerta(successMessage + " ID: " + data.id, 'success');

                limpiarFormulario();

                cargarEstudiantes();

                listTabContent.show();

            } catch (error) {
                console.error("Hubo un error al procesar el estudiante:", error);
                mostrarAlerta("Error al procesar estudiante: " + error.message, 'danger');
            }
        });
    }

    const btnLimpiarFormulario = document.getElementById('btnLimpiarFormulario');
    if (btnLimpiarFormulario) {
        btnLimpiarFormulario.addEventListener('click', limpiarFormulario);
    }

    document.addEventListener('click', async (event) => {
        if (event.target.classList.contains('btn-eliminar')) {
            const confirmar = confirm('¿Estás seguro de que deseas eliminar este estudiante?');
            if (confirmar) {
                const estudianteId = event.target.dataset.id;
                await eliminarEstudiante(estudianteId);
            }
        }
        else if (event.target.classList.contains('btn-editar')) {
            const estudianteId = event.target.dataset.id;
            console.log('Intentando cargar estudiante para edición con ID:', estudianteId);
            await cargarEstudianteEnFormulario(estudianteId);
        }
    });

    formTabBtn.addEventListener('shown.bs.tab', () => {
        if (!modoEdicion) {
            limpiarFormulario();
        }
    });

    // Asegúrate de que los cursos se carguen ANTES de intentar cargar los estudiantes
    cargarCursosParaSelect().then(() => {
        cargarEstudiantes();
    });
});