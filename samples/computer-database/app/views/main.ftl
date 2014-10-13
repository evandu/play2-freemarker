<#escape x as (x)!>
<!DOCTYPE html>
<html>
    <head>
        <title>Computers database</title>

        <link rel='stylesheet' href='${routes.Assets.at("lib/bootstrap/css/bootstrap.min.css")}'>
        <link rel='stylesheet' href='@routes.Assets.at("lib/font-awesome/css/font-awesome.min.css")'>

        <script src="@routes.Assets.at("lib/jquery/jquery.js")" type="text/javascript"></script>
        <script src="@routes.Assets.at("lib/bootstrap/js/bootstrap.min.js")" type="text/javascript"></script>

        <link rel="stylesheet" type="text/css" media="screen" href="@routes.Assets.at("stylesheets/bootstrap.min.css")">

        <link rel="stylesheet" media="screen" href="@routes.Assets.at("stylesheets/main.css")"/>


    </head>
    <body>

        <header class="topbar">
            <h1 class="fill">
                <a href="">
                    Play sample application &mdash; Computer database
                </a>
            </h1>
        </header>

        <section id="main">
             <@block name="content">
             </@block>
        </section>

    </body>
</html>
</#escape>