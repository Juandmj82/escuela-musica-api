// js/profesores.js

// Este bloque se ejecuta cuando todo el DOM (HTML) ha sido cargado y está listo.
document.addEventListener('DOMContentLoaded', () => {

    // URL base de tu API REST para profesores (¡CAMBIADO!)
    const API_URL = 'http://localhost:8080/api/profesores'; // <-- AQUI ES PROFESORES

    // Referencias a elementos clave del DOM (¡TODOS CAMBIADOS!)
    const btnCargarProfesores = document.getElementById('btnCargarProfesores'); // <-- ID de botón
    const formProfesor = document.getElementById('formProfesor'); // <-- ID de formulario
    const cuerpoTablaProfesores = document.getElementById('cuerpoTablaProfesores'); // <-- ID de tbody

    // Campo oculto para guardar el ID del profesor a editar
    const profesorIdInput = document.createElement('input');
    profesorIdInput.type = 'hidden';
    profesorIdInput.id = 'profesorId'; // <-- ID específico para profesor
    profesorIdInput.name = 'id';
    formProfesor.appendChild(profesorIdInput); // Añadirlo al formulario

    let modoEdicion = false; // Variable para controlar si estamos creando o editando

    // --- Funciones auxiliares ---

    // Función para cargar y mostrar profesores en la tabla (GET /api/profesores)
    async function cargarProfesores() { // <-- Nombre de función
        console.log("Iniciando carga de profesores...");
        try {
            // MODIFICACIÓN: Añadir las cabeceras de autorización
            const response = await fetch(API_URL, {
                headers: window.getAuthHeaders() // <-- AÑADIDO
            });

            if (!response.ok) {
                throw new Error(`Error HTTP: ${response.status} - ${response.statusText}`);
            }

            const data = await response.json();
            console.log("Profesores cargados exitosamente:", data);

            cuerpoTablaProfesores.innerHTML = ''; // Limpiar la tabla

            if (data && data.length > 0) {
                data.forEach(profesor => { // <-- Variable 'profesor'
                    const fila = document.createElement('tr');

                    // Crear y añadir celdas para cada propiedad del profesor (¡ADAPTADO A TU ENTIDAD PROFESOR!)
                    const idCelda = document.createElement('td');
                    idCelda.textContent = profesor.id;
                    fila.appendChild(idCelda);

                    const nombreCelda = document.createElement('td');
                    nombreCelda.textContent = profesor.nombre; // <-- Propiedad del profesor
                    fila.appendChild(nombreCelda);

                    const apellidosCelda = document.createElement('td');
                    apellidosCelda.textContent = profesor.apellidos;
                    fila.appendChild(apellidosCelda);

                    const emailCelda = document.createElement('td');
                    emailCelda.textContent = profesor.email;
                    fila.appendChild(emailCelda);

                    // NUEVOS CAMPOS DEL PROFESOR
                    const telefonoCelda = document.createElement('td');
                    telefonoCelda.textContent = profesor.telefono;
                    fila.appendChild(telefonoCelda);

                    const instrumentoCelda = document.createElement('td');
                    instrumentoCelda.textContent = profesor.instrumento;
                    fila.appendChild(instrumentoCelda);

                    const modalidadCelda = document.createElement('td');
                    modalidadCelda.textContent = profesor.modalidad;
                    fila.appendChild(modalidadCelda);
                    // FIN NUEVOS CAMPOS

                    const accionesCelda = document.createElement('td');

                    const btnEditar = document.createElement('button');
                    btnEditar.textContent = 'Editar';
                    btnEditar.classList.add('btn', 'btn-info', 'btn-sm', 'me-1', 'btn-editar');
                    btnEditar.dataset.id = profesor.id; // <-- Usamos id del profesor
                    accionesCelda.appendChild(btnEditar);

                    const btnEliminar = document.createElement('button');
                    btnEliminar.textContent = 'Eliminar';
                    btnEliminar.classList.add('btn', 'btn-danger', 'btn-sm', 'btn-eliminar');
                    btnEliminar.dataset.id = profesor.id; // <-- Usamos id del profesor
                    accionesCelda.appendChild(btnEliminar);

                    fila.appendChild(accionesCelda);
                    cuerpoTablaProfesores.appendChild(fila); // <-- Usamos el tbody de profesores
                });
            } else {
                const filaVacia = document.createElement('tr');
                const celdaMensaje = document.createElement('td');
                celdaMensaje.colSpan = 8; // <-- ¡IMPORTANTE! Ajustar a 8 columnas de la tabla de profesores
                celdaMensaje.textContent = 'No hay profesores registrados aún.';
                celdaMensaje.classList.add('text-center', 'text-muted');
                filaVacia.appendChild(celdaMensaje);
                cuerpoTablaProfesores.appendChild(filaVacia);
            }
        } catch (error) {
            console.error("Hubo un error al cargar los profesores:", error);
            alert("Error al cargar profesores: " + error.message);
        }
    }

    // Función para eliminar un profesor (DELETE /api/profesores/{id})
    async function eliminarProfesor(id) { // <-- Nombre de función
        try {
            // MODIFICACIÓN: Añadir las cabeceras de autorización
            const response = await fetch(`${API_URL}/${id}`, {
                method: 'DELETE',
                headers: window.getAuthHeaders() // <-- AÑADIDO
            });

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(`Error al eliminar: ${response.status} - ${response.statusText}. Detalle: ${errorText}`);
            }

            console.log(`Profesor con ID ${id} eliminado exitosamente.`);
            alert(`Profesor con ID ${id} eliminado exitosamente.`);

            cargarProfesores(); // Recargar la tabla

        } catch (error) {
            console.error("Hubo un error al eliminar el profesor:", error);
            alert("Error al eliminar profesor: " + error.message);
        }
    }

    // Función para cargar los datos de un profesor en el formulario para edición (GET /api/profesores/{id})
    async function cargarProfesorEnFormulario(id) { // <-- Nombre de función
        try {
            // MODIFICACIÓN: Añadir las cabeceras de autorización
            const response = await fetch(`${API_URL}/${id}`, {
                headers: window.getAuthHeaders() // <-- AÑADIDO
            });
            if (!response.ok) {
                throw new Error(`Error al cargar profesor para editar: ${response.status} - ${response.statusText}`);
            }
            const profesor = await response.json(); // <-- Variable 'profesor'
            console.log("Profesor cargado para edición:", profesor);

            // Rellenar el formulario con los datos del profesor (¡ADAPTADO A TU ENTIDAD PROFESOR!)
            document.getElementById('nombre').value = profesor.nombre;
            document.getElementById('apellidos').value = profesor.apellidos;
            document.getElementById('email').value = profesor.email;
            document.getElementById('telefono').value = profesor.telefono; // <-- Nuevo campo
            document.getElementById('instrumento').value = profesor.instrumento; // <-- Nuevo campo
            document.getElementById('modalidad').value = profesor.modalidad; // <-- Nuevo campo

            profesorIdInput.value = profesor.id; // <-- ID de profesor en campo oculto
            modoEdicion = true;
            formProfesor.querySelector('button[type="submit"]').textContent = 'Actualizar Profesor'; // <-- Texto del botón
            formProfesor.scrollIntoView({ behavior: 'smooth' }); // Desplazarse al formulario

            const formTabButton = document.getElementById('form-tab');
            if (formTabButton) {
                const bsTab = new bootstrap.Tab(formTabButton);
                bsTab.show();
            }

        } catch (error) {
            console.error("Error cargando profesor para edición:", error);
            alert("Error al cargar profesor para edición: " + error.message);
        }
    }

    // --- Manejadores de Eventos Principales ---

    // Evento para el botón "Cargar Profesores"
    if (btnCargarProfesores) { // <-- Nombre de botón
        btnCargarProfesores.addEventListener('click', cargarProfesores); // <-- Nombre de función
    }

    // Evento para el envío del formulario (Crear o Actualizar)
    if (formProfesor) { // <-- Nombre de formulario
        formProfesor.addEventListener('submit', async (event) => {
            event.preventDefault();

            // 1. Obtener los valores de los campos del formulario (¡ADAPTADO A TU ENTIDAD PROFESOR!)
            const id = profesorIdInput.value; // <-- ID de profesor
            const nombre = document.getElementById('nombre').value;
            const apellidos = document.getElementById('apellidos').value;
            const email = document.getElementById('email').value;
            const telefono = document.getElementById('telefono').value; // <-- Nuevo campo
            const instrumento = document.getElementById('instrumento').value; // <-- Nuevo campo
            const modalidad = document.getElementById('modalidad').value; // <-- Nuevo campo

            // Crea un objeto con los datos del profesor (¡ADAPTADO A TU ENTIDAD PROFESOR!)
            const profesorData = {
                nombre: nombre,
                apellidos: apellidos,
                email: email,
                telefono: telefono,
                instrumento: instrumento,
                modalidad: modalidad
            };

            let method = 'POST';
            let url = API_URL;
            let successMessage = "Profesor guardado exitosamente."; // <-- Mensaje

            // Si estamos en modo edición, cambiamos el método a PUT y la URL
            if (modoEdicion) {
                method = 'PUT';
                url = `${API_URL}/${id}`;
                successMessage = "Profesor actualizado exitosamente."; // <-- Mensaje
                console.log("Actualizando profesor con ID:", id, "Datos:", profesorData);
            } else {
                console.log("Creando nuevo profesor. Datos:", profesorData);
            }

            // Realizar la petición (POST o PUT)
            try {
                const response = await fetch(url, {
                    method: method,
                    headers: {
                        'Content-Type': 'application/json',
                        ...window.getAuthHeaders() // <-- AÑADIDO
                    },
                    body: JSON.stringify(profesorData) // <-- Enviamos datos de profesor
                });

                if (!response.ok) {
                    let errorData;
                    try {
                        errorData = await response.json();
                    } catch (e) {
                        throw new Error(`Error HTTP: ${response.status} - ${response.statusText}`);
                    }
                    throw new Error(JSON.stringify(errorData) || 'Error al procesar el profesor'); // <-- Mensaje
                }

                const data = await response.json();
                console.log(successMessage, data);
                alert(successMessage + " ID: " + data.id);

                // Limpiar el formulario y resetear el modo edición
                formProfesor.reset(); // <-- Resetear formulario
                profesorIdInput.value = ''; // <-- Limpiar ID oculto
                modoEdicion = false;
                formProfesor.querySelector('button[type="submit"]').textContent = 'Guardar Profesor'; // <-- Texto del botón

                cargarProfesores(); // Recargar la tabla

                const listTabButton = document.getElementById('list-tab');
                if (listTabButton) {
                    const bsTab = new bootstrap.Tab(listTabButton);
                    bsTab.show(); // Mostrar la pestaña de listado
                }

            } catch (error) {
                console.error("Hubo un error al procesar el profesor:", error);
                let errorMessage = "Error desconocido al procesar profesor."; // <-- Mensaje
                try {
                    const parsedError = JSON.parse(error.message);
                    errorMessage = Object.values(parsedError).join(', ');
                } catch (e) {
                    errorMessage = error.message;
                }
                alert("Error: " + errorMessage);
            }
        });
    }

    // Delegación de Eventos para botones "Editar" y "Eliminar" (generados dinámicamente)
    document.addEventListener('click', async (event) => {
        // Si se hizo clic en un botón "Eliminar"
        if (event.target.classList.contains('btn-eliminar')) {
            const confirmar = confirm('¿Estás seguro de que deseas eliminar este profesor?'); // <-- Mensaje
            if (confirmar) {
                const profesorId = event.target.dataset.id;
                await eliminarProfesor(profesorId); // <-- Función eliminar
            }
        }
        // Si se hizo clic en un botón "Editar"
        else if (event.target.classList.contains('btn-editar')) {
            const profesorId = event.target.dataset.id;
            console.log('Intentando cargar profesor para edición con ID:', profesorId); // <-- Mensaje
            await cargarProfesorEnFormulario(profesorId); // <-- Función cargar en formulario
        }
    });

    // --- Carga inicial de profesores al cargar la página ---
    cargarProfesores(); // <-- Carga inicial

}); // Fin de DOMContentLoaded