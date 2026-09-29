// src/main/resources/static/js/register.js

document.addEventListener('DOMContentLoaded', function() {
    const registerForm = document.getElementById('registerForm');
    const messageDiv = document.getElementById('message');

    if (registerForm) {
        registerForm.addEventListener('submit', async function(event) {
            event.preventDefault(); // Evita el envío tradicional del formulario

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            messageDiv.className = 'mt-3'; // Resetear clases
            messageDiv.textContent = ''; // Limpiar mensaje anterior

            // Validaciones básicas del lado del cliente
            if (password !== confirmPassword) {
                messageDiv.classList.add('alert', 'alert-danger');
                messageDiv.textContent = 'Las contraseñas no coinciden.';
                return; // Detener la ejecución si las contraseñas no coinciden
            }

            if (password.length < 6) { // Ejemplo: mínimo 6 caracteres para la contraseña
                messageDiv.classList.add('alert', 'alert-danger');
                messageDiv.textContent = 'La contraseña debe tener al menos 6 caracteres.';
                return;
            }

            try {
                const response = await fetch('http://localhost:8080/api/auth/register', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        nombreUsuario: username, // Asegúrate que coincida con tu RegisterRequest
                        contrasena: password,    // Asegúrate que coincida con tu RegisterRequest
                        // roles: ["user"] // Puedes enviar roles aquí si tu backend lo soporta y lo quieres forzar
                    })
                });

                const data = await response.json(); // Leer siempre la respuesta para mensajes de éxito/error

                if (response.ok) {
                    messageDiv.classList.add('alert', 'alert-success');
                    messageDiv.textContent = data.message || '¡Registro exitoso! Ahora puedes iniciar sesión.';
                    // Opcional: limpiar el formulario después de un registro exitoso
                    registerForm.reset();
                    // Opcional: redirigir al usuario a la página de login después de un tiempo
                    setTimeout(() => {
                        window.location.href = 'http://localhost:8080/login.html';
                    }, 3000); // Redirige después de 3 segundos
                } else {
                    messageDiv.classList.add('alert', 'alert-danger');
                    messageDiv.textContent = data.message || 'Error en el registro. Intenta de nuevo.';
                }
            } catch (error) {
                console.error('Error durante el registro:', error);
                messageDiv.classList.add('alert', 'alert-danger');
                messageDiv.textContent = 'Hubo un error al intentar registrarte. Por favor, inténtalo más tarde.';
            }
        });
    }
});