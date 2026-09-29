// js/login.js  (o donde sea que lo tengas dentro de tu proyecto de VSC)

document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const messageDiv = document.getElementById('errorMessage');

    if (loginForm) {
        loginForm.addEventListener('submit', async function(event) {
            event.preventDefault(); // Evita el envío tradicional del formulario

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            // Limpiar mensajes anteriores y ocultar
            messageDiv.textContent = '';
            messageDiv.classList.add('d-none');

            // Validaciones básicas del lado del cliente
            if (!username || !password) {
                messageDiv.classList.remove('d-none');
                messageDiv.textContent = 'Por favor, ingresa tu usuario y contraseña.';
                return;
            }

            try {
                const response = await fetch('http://localhost:8080/api/auth/login', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        nombreUsuario: username, // Debe coincidir con LoginRequest.java
                        contrasena: password     // Debe coincidir con LoginRequest.java
                    })
                });

                const data = await response.json();

                if (response.ok) {
                    messageDiv.classList.remove('alert-danger');
                    messageDiv.classList.add('alert-success');
                    messageDiv.textContent = data.message || '¡Inicio de sesión exitoso!';
                    messageDiv.classList.remove('d-none');

                    if (data.token) {
                        localStorage.setItem('jwtToken', data.token); // Guarda el token JWT

                        // *** MODIFICACIÓN CLAVE: Guarda los roles del usuario también ***
                        // Los roles vienen en la propiedad 'roles' de la respuesta JSON (fuera del JWT)
                        if (data.roles && Array.isArray(data.roles)) {
                            localStorage.setItem('userRoles', JSON.stringify(data.roles)); // Guarda los roles como una cadena JSON
                        } else {
                            console.warn('Inicio de sesión exitoso, pero no se recibieron roles válidos. Se guardará un array vacío.');
                            localStorage.setItem('userRoles', JSON.stringify([])); // Asegura que siempre haya un array (aunque vacío)
                        }

                        // Redirigir al dashboard después de un breve retraso
                        setTimeout(() => {
                            window.location.href = 'http://localhost:8080/dashboard.html';
                        }, 1500);
                    } else {
                        console.warn('Inicio de sesión exitoso, pero no se recibió el token JWT.');
                        setTimeout(() => {
                            window.location.href = 'http://localhost:8080/dashboard.html'; // Redirige aunque no haya token si es el comportamiento deseado
                        }, 1500);
                    }
                } else {
                    // Si la respuesta no es OK (ej. 401 Unauthorized, 400 Bad Request)
                    messageDiv.classList.remove('alert-success');
                    messageDiv.classList.add('alert-danger');
                    messageDiv.textContent = data.message || 'Error en el inicio de sesión. Credenciales inválidas o cuenta no activa.';
                    messageDiv.classList.remove('d-none');
                }
            } catch (error) {
                console.error('Error durante el inicio de sesión:', error);
                messageDiv.classList.remove('alert-success');
                messageDiv.classList.add('alert-danger');
                messageDiv.textContent = 'Hubo un error de red o de servidor al intentar iniciar sesión. Por favor, inténtalo más tarde.';
                messageDiv.classList.remove('d-none');
            }
        });
    }
});