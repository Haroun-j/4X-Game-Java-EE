<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Connexion et Inscription</title>
    <link rel="stylesheet" href="styles/login.css">
</head>
<body>
<div class="form-container">
    <%
        String success = request.getParameter("success");
        String error = request.getParameter("error");
        String sessionId = request.getSession().getId();
    %>
    <h1>Connexion et Inscription</h1>
    <div class="form-wrapper">
        <!-- Subscription Form -->
        <div class="form-box">
            <h2>Inscription</h2>
            <form action="LoginController" method="POST">
                <input type="hidden" name="action" value="subscribe">
                <input type="hidden" name="sessionId" value="<%= sessionId %>">
                <div class="form-group">
                    <label for="email">Email:</label>
                    <input type="email" id="email" name="email" placeholder="Entrez votre email" required>
                </div>
                <div class="form-group">
                    <label for="username">Nom d'utilisateur:</label>
                    <input type="text" id="username" name="username" placeholder="Entrez votre nom d'utilisateur" required>
                </div>
                <div class="form-group">
                    <label for="pseudo">Pseudo:</label>
                    <input type="text" id="pseudo" name="pseudo" placeholder="Entrez votre pseudo" required>
                </div>
                <div class="form-group">
                    <label for="password">Mot de passe:</label>
                    <input type="password" id="password" name="password" placeholder="Entrez votre mot de passe" required>
                </div>
                <div class="form-group">
                    <label for="sexe">Sexe:</label>
                    <select id="sexe" name="sexe" required>
                        <option value="">Sélectionnez...</option>
                        <option value="Homme">Homme</option>
                        <option value="Femme">Femme</option>
                    </select>
                </div>
                <button type="submit" class="submit-btn">S'inscrire</button>
            </form>
        </div>

        <!-- Login Form -->
        <div class="form-box">
            <h2>Connexion</h2>
            <form action="LoginController" method="POST">
                <input type="hidden" name="action" value="login">
                <input type="hidden" name="sessionId" value="<%= sessionId %>">
                <div class="form-group">
                    <label for="login-email">Email:</label>
                    <input type="email" id="login-email" name="email" placeholder="Entrez votre email" required>
                </div>
                <div class="form-group">
                    <label for="login-password">Mot de passe:</label>
                    <input type="password" id="login-password" name="password" placeholder="Entrez votre mot de passe" required>
                </div>
                <button type="submit" class="submit-btn">Se connecter</button>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<% if ("1".equals(success)) { %>
<script>
    Swal.fire({
        title: 'Inscription réussie!',
        text: 'Vous êtes bien inscrit. Veuillez vous connecter pour continuer.',
        icon: 'success',
        confirmButtonText: 'OK'
    });
</script>
<% } else if ("subscription_failed".equals(error)) { %>
<script>
    Swal.fire({
        title: 'Erreur!',
        text: 'Une erreur est survenue lors de l\'inscription. Veuillez réessayer.',
        icon: 'error',
        confirmButtonText: 'OK'
    });
</script>
<% } else if ("invalid_credentials".equals(error)) { %>
<script>
    Swal.fire({
        title: 'Identifiants incorrects!',
        text: 'L\'email ou le mot de passe est incorrect.',
        icon: 'error',
        confirmButtonText: 'OK'
    });
</script>
<% } else if ("login_failed".equals(error)) { %>
<script>
    Swal.fire({
        title: 'Erreur de connexion!',
        text: 'Impossible de se connecter. Veuillez réessayer plus tard.',
        icon: 'error',
        confirmButtonText: 'OK'
    });
</script>
<% } %>
</body>
</html>
