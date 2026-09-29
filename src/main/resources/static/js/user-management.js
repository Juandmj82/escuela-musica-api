// js/user-management.js

document.addEventListener('DOMContentLoaded', () => {

    // URL base de la API de usuarios (que ya está protegida en el backend)
    const API_USERS_URL = 'http://localhost:8080/api/users';

    // Elementos del DOM
    const btnCargarUsuarios = document.getElementById('btnCargarUsuarios');
    const cuerpoTablaUsuarios = document.getElementById('cuerpoTablaUsuarios');
    const formUsuario = document.getElementById('formUsuario');
    const userIdInput = document.getElementById('userId');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const rolesSelect = document.getElementById('roles'); // Select para múltiples roles

    // Elementos de las pestañas
    const userListTabButton = document.getElementById('user-list-tab');
    const userFormTabButton = document.getElementById('user-form-tab');

    // Estado del formulario
    let modoEdicion = false;

    // Función auxiliar para mostrar alertas
    function mostrarAlerta(mensaje, tipo = 'info') {
        const alertContainer = document.getElementById('alertContainer');
        if (alertContainer) {
            // Limpiar alertas previas
            alertContainer.innerHTML = '';
            const alertDiv = document.createElement('div');
            alertDiv.classList.add('alert', `alert-${tipo}`, 'alert-dismissible', 'fade', 'show');
            alertDiv.setAttribute('role', 'alert');
            alertDiv.innerHTML = `
                ${mensaje}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            `;
            alertContainer.appendChild(alertDiv);
        } else {
            console.warn("Contenedor de alertas no encontrado. Mensaje: " + mensaje);
            alert(mensaje); // Fallback a alert() si no hay contenedor
        }
    }

    // Función para cargar y mostrar la lista de usuarios
    async function cargarUsuarios() {
        console.log("Cargando usuarios desde el backend...");
        try {
            const response = await fetch(API_USERS_URL, {
                headers: window.getAuthHeaders() // Usa la función global para el token
            });

            if (!response.ok) {
                // Manejo de errores para cargar usuarios (simple, ya que no es el problema principal)
                const errorBody = await response.text();
                throw new Error(`Error HTTP al cargar usuarios: ${response.status} - ${response.statusText}. Detalle: ${errorBody}`);
            }

            const users = await response.json();
            console.log("Usuarios cargados exitosamente:", users);

            cuerpoTablaUsuarios.innerHTML = ''; // Limpiar la tabla antes de añadir nuevas filas

            if (users && users.length > 0) {
                users.forEach(user => {
                    const fila = document.createElement('tr');
                    // Asegúrate de que user.roles es un array de objetos con una propiedad 'name'
                    // Si tu backend envía solo strings de roles, ajusta: user.roles.join(', ')
                    // O si devuelve un array de objetos con un campo 'nombre', ajusta a role.nombre
                    const rolesDisplay = user.roles ? user.roles.map(role => role.name || role.nombre || role).join(', ') : 'N/A';

                    fila.innerHTML = `
                        <td>${user.id}</td>
                        <td>${user.nombreUsuario}</td>
                        <td>${rolesDisplay}</td>
                        <td>
                            <button class="btn btn-info btn-sm me-1 btn-editar-usuario" data-id="${user.id}">Editar</button>
                            <button class="btn btn-danger btn-sm btn-eliminar-usuario" data-id="${user.id}">Eliminar</button>
                        </td>
                    `;
                    cuerpoTablaUsuarios.appendChild(fila);
                });
            } else {
                cuerpoTablaUsuarios.innerHTML = '<tr><td colspan="4" class="text-center text-muted">No hay usuarios registrados.</td></tr>';
            }

        } catch (error) {
            console.error("Hubo un error al cargar los usuarios:", error);
            mostrarAlerta("Error al cargar usuarios: " + error.message, 'danger');
        }
    }

    // Función para cargar un usuario en el formulario para edición
    async function cargarUsuarioEnFormulario(id) {
        try {
            const response = await fetch(`${API_USERS_URL}/${id}`, {
                headers: window.getAuthHeaders()
            });
            if (!response.ok) {
                const errorBody = await response.text();
                throw new Error(`Error al cargar usuario para edición: ${response.status} - ${response.statusText}. Detalle: ${errorBody}`);
            }
            const user = await response.json();
            console.log("Usuario cargado para edición:", user);

            userIdInput.value = user.id;
            usernameInput.value = user.nombreUsuario;
            passwordInput.value = ''; // No precargar la contraseña por seguridad

            // Seleccionar los roles correctos en el <select multiple>
            // Ajusta aquí si user.roles es un array de objetos con 'nombre' o solo strings
            const userRoles = user.roles.map(role => role.name || role.nombre || role);
            Array.from(rolesSelect.options).forEach(option => {
                option.selected = userRoles.includes(option.value);
            });

            modoEdicion = true;
            formUsuario.querySelector('button[type="submit"]').textContent = 'Actualizar Usuario';

            // Cambiar a la pestaña del formulario
            const bsTab = new bootstrap.Tab(userFormTabButton);
            bsTab.show();

        } catch (error) {
            console.error("Error cargando usuario para edición:", error);
            mostrarAlerta("Error al cargar usuario para edición: " + error.message, 'danger');
        }
    }

    // Función para eliminar un usuario
    async function eliminarUsuario(id) {
        const confirmar = confirm(`¿Estás seguro de que deseas eliminar al usuario con ID ${id}? Esta acción es irreversible.`);
        if (!confirmar) return;

        try {
            const response = await fetch(`${API_USERS_URL}/${id}`, {
                method: 'DELETE',
                headers: window.getAuthHeaders()
            });

            if (!response.ok) {
                // Manejo de errores para eliminar (simple, ya que no es el problema principal)
                const errorBody = await response.text();
                throw new Error(`Error al eliminar usuario: ${response.status} - ${response.statusText}. Detalle: ${errorBody}`);
            }
            mostrarAlerta(`Usuario con ID ${id} eliminado exitosamente.`, 'success');
            cargarUsuarios(); // Recargar la lista de usuarios
        } catch (error) {
            console.error("Error al eliminar usuario:", error);
            mostrarAlerta("Error al eliminar usuario: " + error.message, 'danger');
        }
    }

    // Función para limpiar el formulario
    function limpiarFormulario() {
        formUsuario.reset();
        userIdInput.value = '';
        modoEdicion = false;
        formUsuario.querySelector('button[type="submit"]').textContent = 'Guardar Usuario';
        // Deseleccionar todos los roles del select múltiple
        Array.from(rolesSelect.options).forEach(option => {
            option.selected = false;
        });
        // Opcional: seleccionar un rol por defecto para la creación, si lo deseas
        // rolesSelect.value = 'USER';
    }

    // --- Event Listeners ---

    // Cargar usuarios al hacer clic en el botón
    if (btnCargarUsuarios) {
        btnCargarUsuarios.addEventListener('click', cargarUsuarios);
    }

    // Manejar el envío del formulario (crear o actualizar)
    if (formUsuario) {
        formUsuario.addEventListener('submit', async (event) => {
            event.preventDefault();

            const id = userIdInput.value;
            const username = usernameInput.value;
            const password = passwordInput.value;

            // Obtener todos los roles seleccionados del select múltiple
            const selectedRoles = Array.from(rolesSelect.selectedOptions).map(option => option.value);

            // Objeto de datos a enviar al backend, mapeando a los nombres esperados por RegisterRequest
            let userData = {
                nombreUsuario: username, // Mapea 'username' del frontend a 'nombreUsuario' del backend
                roles: selectedRoles     // Esto coincide 'roles'
            };

            // Solo incluye la contraseña si se ha ingresado algo (para creación o cambio)
            if (password) {
                userData.contrasena = password; // Mapea 'password' del frontend a 'contrasena' del backend
            } else if (!modoEdicion) {
                // En modo creación, la contraseña es obligatoria si no se envía por defecto
                mostrarAlerta('La contraseña es obligatoria para un nuevo usuario.', 'warning');
                return;
            }

            let method = 'POST';
            let url = API_USERS_URL;
            let successMessage = "Usuario guardado exitosamente.";

            if (modoEdicion) {
                method = 'PUT';
                url = `${API_USERS_URL}/${id}`;
                successMessage = "Usuario actualizado exitosamente.";
                console.log("Actualizando usuario con ID:", id, "Datos:", userData);
            } else {
                console.log("Creando nuevo usuario. Datos:", userData);
            }

            try {
                const response = await fetch(url, {
                    method: method,
                    headers: {
                        'Content-Type': 'application/json',
                        ...window.getAuthHeaders() // Añade el token JWT
                    },
                    body: JSON.stringify(userData)
                });

                // --- INICIO DEL BLOQUE DE MANEJO DE ERRORES CORREGIDO ---
                if (!response.ok) {
                    let errorMessage = `Error HTTP: ${response.status} - ${response.statusText}`;
                    let errorBody = '';

                    // Intentar leer el cuerpo de la respuesta como texto una SOLA VEZ.
                    try {
                        errorBody = await response.text();
                    } catch (e) {
                        console.warn("No se pudo leer el cuerpo de la respuesta:", e);
                    }

                    // Ahora, intentar parsear el texto a JSON si no está vacío
                    if (errorBody) {
                        try {
                            const errorJson = JSON.parse(errorBody);
                            if (errorJson && errorJson.message) {
                                errorMessage = errorJson.message;
                            } else if (errorJson && errorJson.errors && Array.isArray(errorJson.errors)) {
                                const messages = errorJson.errors.map(err => err.defaultMessage || err.field + ': ' + err.code);
                                errorMessage = "Errores de validación: " + messages.join(', ');
                            } else {
                                errorMessage = `Error: ${JSON.stringify(errorJson)}`;
                            }
                        } catch (jsonParseError) {
                            errorMessage = `${errorMessage}. Detalle: ${errorBody}`;
                        }
                    } else {
                        errorMessage = `${errorMessage}. No hay detalles adicionales en la respuesta del servidor.`;
                    }

                    throw new Error(errorMessage);
                }
                // --- FIN DEL BLOQUE DE MANEJO DE ERRORES CORREGIDO ---

                // Si la respuesta es OK (2xx), procesar el JSON de éxito
                const data = await response.json();
                console.log(successMessage, data);
                mostrarAlerta(successMessage + (data.id ? " ID: " + data.id : ""), 'success');

                limpiarFormulario();
                cargarUsuarios(); // Recargar la lista después de la operación

                // Cambiar a la pestaña de listado después de guardar/actualizar
                const bsTab = new bootstrap.Tab(userListTabButton);
                bsTab.show();

            } catch (error) {
                console.error("Hubo un error al procesar el usuario:", error);
                mostrarAlerta("Error al procesar usuario: " + error.message, 'danger');
            }
        });
    }

    // Botón para limpiar el formulario
    const btnLimpiarUserForm = document.getElementById('btnLimpiarUserForm');
    if (btnLimpiarUserForm) {
        btnLimpiarUserForm.addEventListener('click', limpiarFormulario);
    }

    // Manejar clics en los botones de la tabla (editar y eliminar)
    document.addEventListener('click', async (event) => {
        if (event.target.classList.contains('btn-eliminar-usuario')) {
            const userId = event.target.dataset.id;
            await eliminarUsuario(userId);
        } else if (event.target.classList.contains('btn-editar-usuario')) {
            const userId = event.target.dataset.id;
            await cargarUsuarioEnFormulario(userId);
        }
    });

    // Lógica para limpiar el formulario al cambiar a la pestaña de formulario (si no estamos editando)
    userFormTabButton.addEventListener('shown.bs.tab', () => {
        if (!modoEdicion) {
            limpiarFormulario();
        }
    });

    // Carga inicial de usuarios al cargar la página (solo si el usuario es ADMIN)
    // Se recomienda comprobar el rol aquí para evitar llamadas innecesarias si el usuario no es ADMIN
    if (window.getUserRoles && window.getUserRoles().includes('ADMIN')) {
        cargarUsuarios();
    }
});