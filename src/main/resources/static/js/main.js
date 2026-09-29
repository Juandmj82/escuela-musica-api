// js/main.js

document.addEventListener('DOMContentLoaded', function() {
    // Referencias a elementos del DOM (AHORA USANDO LOS IDs CORRECTOS DE TUS HTML)
    const logoutButton = document.getElementById('logoutButton');
    const navInicioLink = document.getElementById('navInicio'); // Corregido: ID para "Inicio"
    const navEstudiantesLink = document.getElementById('navEstudiantes'); // Corregido: ID para "Estudiantes"
    const navProfesoresLink = document.getElementById('navProfesores'); // Corregido: ID para "Profesores"
    const navCursosLink = document.getElementById('navCursos'); // Corregido: ID para "Cursos"

    // Estos IDs solo existen en dashboard.html, por lo que necesitan checks de null
    const welcomeMessageDiv = document.getElementById('welcomeMessage');
    const adminFeaturesDiv = document.getElementById('adminFeatures');
    const userFeaturesDiv = document.getElementById('userFeatures');

    // Función básica para decodificar la parte del payload de un JWT
    function parseJwt(token) {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
                return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
            }).join(''));
            return JSON.parse(jsonPayload);
        } catch (e) {
            console.error("Error decodificando JWT en parseJwt:", e);
            return null;
        }
    }

    // Función para verificar roles (puede ser útil fuera de DOMContentLoaded si se necesita)
    // Se ha movido aquí para ser accesible globalmente si es necesario
    window.hasRole = function(role) {
        const storedRoles = localStorage.getItem('userRoles');
        if (storedRoles) {
            try {
                const userRoles = JSON.parse(storedRoles);
                return userRoles.includes(role);
            } catch (e) {
                console.error("Error parseando roles desde localStorage en hasRole:", e);
                return false;
            }
        }
        return false;
    };

    // NUEVA FUNCIÓN: Obtener cabeceras de autorización con JWT
    function getAuthHeaders() {
        const token = localStorage.getItem('jwtToken');
        if (token) {
            return {
                'Content-Type': 'application/json',
                'Authorization': 'Bearer ' + token
            };
        } else {
            // Si no hay token, redirigir al login o manejar el error
            console.warn("No hay token JWT en localStorage. Redirigiendo a login.");
            window.location.href = '/login.html'; // Redirige si no hay token
            return {}; // Devuelve un objeto vacío si no hay token
        }
    }
    // Hacer que la función sea globalmente accesible
    window.getAuthHeaders = getAuthHeaders;


    // 1. Verificar Token y Obtener Roles (AHORA DE LOCALSTORAGE)
    const jwtToken = localStorage.getItem('jwtToken');
    let userRoles = [];
    let username = '';

    // Intentar obtener los roles del localStorage (donde login.js los guardó)
    const storedRoles = localStorage.getItem('userRoles');
    if (storedRoles) {
        try {
            userRoles = JSON.parse(storedRoles);
        } catch (e) {
            console.error("Error parseando roles desde localStorage:", e);
            userRoles = []; // Si hay un error, asegurar un array vacío
        }
    }

    // Si no hay token JWT, redirigir al login
    if (!jwtToken) {
        console.log("No hay token JWT, redirigiendo al login.");
        window.location.href = '/login.html';
        return; // Detener la ejecución
    } else {
        const decodedToken = parseJwt(jwtToken);
        if (decodedToken && decodedToken.sub) {
            username = decodedToken.sub; // El username (subject) siempre debería estar en el JWT

            // Actualizar mensaje de bienvenida (si el elemento existe en la página)
            if (welcomeMessageDiv) {
                welcomeMessageDiv.textContent = `¡Hola, ${username}! Has iniciado sesión correctamente.`;
            }

            // 2. Ajustar Navbar y Contenido según el Rol
            const isAdmin = userRoles.includes('ROLE_ADMIN');
            const isUser = userRoles.includes('ROLE_USER'); // Asumimos que un ADMIN también es USER para propósitos de visualización de cursos

            // Lógica de visibilidad para los elementos de navegación
            // Ocultar todos por defecto y luego mostrar los que correspondan
            if (navInicioLink) navInicioLink.style.display = 'none';
            if (navEstudiantesLink) navEstudiantesLink.style.display = 'none';
            if (navProfesoresLink) navProfesoresLink.style.display = 'none';
            if (navCursosLink) navCursosLink.style.display = 'none';

            // Siempre mostrar el enlace a Inicio si hay sesión
            if (navInicioLink) navInicioLink.style.display = '';

            // Mostrar el enlace a Cursos si el usuario tiene algún rol
            if ((isAdmin || isUser) && navCursosLink) {
                navCursosLink.style.display = '';
            }

            // Mostrar/Ocultar elementos específicos de ADMINISTRADOR
            if (isAdmin) {
                if (navEstudiantesLink) navEstudiantesLink.style.display = '';
                if (navProfesoresLink) navProfesoresLink.style.display = '';
                if (adminFeaturesDiv) adminFeaturesDiv.classList.remove('d-none');
                if (userFeaturesDiv) userFeaturesDiv.classList.add('d-none');
            } else if (isUser) { // Si es solo un usuario normal (no admin)
                if (userFeaturesDiv) userFeaturesDiv.classList.remove('d-none');
                if (adminFeaturesDiv) adminFeaturesDiv.classList.add('d-none');
            }

        } else {
            // Si el token es inválido o no tiene 'sub', limpiar y redirigir
            console.error("Token JWT inválido o corrupto, limpiando y redirigiendo.");
            localStorage.removeItem('jwtToken');
            localStorage.removeItem('userRoles'); // Limpiar también los roles guardados
            window.location.href = '/login.html';
            return;
        }
    }

    // 3. Manejar Cerrar Sesión
    // Esta parte ya estaba bien, solo la mantengo para claridad
    if (logoutButton) {
        logoutButton.addEventListener('click', function() {
            localStorage.removeItem('jwtToken'); // Elimina el token JWT
            localStorage.removeItem('userRoles'); // Elimina también los roles guardados
            window.location.href = '/login.html'; // Redirige al login
        });
    }

    // Lógica para el botón "Inicio" del navbar (ya no es estrictamente necesaria si el href es directo)
    // Eliminado el código que seleccionaba por href, ya que navInicioLink ya se gestiona arriba.
});