*** Settings ***
Library           RequestsLibrary
Library           Collections
Library           BuiltIn
Library           SeleniumLibrary
Library           XML

*** Variables ***
${BASE_URL}                http://localhost:9000
${EMAIL}                   admin@gmail.com
${PASSWORD}                12345678
${NAME_USER}               Locatario Robot
${EMAIL_USER}              robot@gmail.com
${PASSWORD_USER}           12345678
${NAME_USER_UPDATE}        Locatario Robot Atualizacao
${EMAIL_USER_UPDATE}       robotAtualizacao@gmail.com
${NAME_ADMIN}              Administrador Robot
${EMAIL_ADMIN}             robotAdministradorRobot@gmail.com
${PASSWORD_ADMIN}          12345678
${EDGE_OPTIONS}            --headless --disable-dev-shm-usage --no-sandbox --remote-debugging-port=9222
${BROWSER}                 Edge

*** Test Cases ***
Usuario Locatario e Admin
    Login
    Criar Usuario Locatario
    Ver Sobre Usuario Locatario
    Editar Usuario Locatario
    Criar Usuario Admin
    Ver Sobre Usuario Admin
    Teste de Pesquisa Locatario
    Teste de Pesquisa Admin

*** Keywords ***

Login
    Open Browser  http://localhost:9000  ${BROWSER}  options=${EDGE_OPTIONS}
    Maximize Browser Window
    Sleep    1

    Wait Until Element Is Visible    css=[itemid="emailInput"]    timeout=10s
    Click Element    css=[itemid="emailInput"]
    Sleep    0.5

    Element Should Be Enabled        css=[itemid="emailInput"]
    Input Text    css=[itemid="emailInput"]    ${EMAIL}
    Sleep    0.5
    
    Wait Until Element Is Visible    css=[itemid="passwordInput"]    timeout=10s
    Click Element    css=[itemid="passwordInput"]
    Sleep    0.5

    Element Should Be Enabled        css=[itemid="passwordInput"]
    Input Text    css=[itemid="passwordInput"]    ${PASSWORD}
    Sleep    0.5

    Click Element    css=[itemid="loginBtn"]
    Sleep    2

Click Item In Menu
    [Arguments]    ${menu_item}
    ${itemid}=    Set Variable    menu-item-${menu_item.lower().replace(" ", "-")}
    Click Element    css=[itemid=${itemid}]

Criar Usuario Locatario
    Click Item In Menu    Usuário
    Sleep    0.5

    Wait Until Element Is Visible    css=[itemid="menu-item-home"]    timeout=10s
    Sleep    1

    Click Button    css=[itemid="cadastroBtnUsuario"]
    Sleep    0.5
    Input Text    css=[itemid="cadastroNomeUsuario"]    ${NAME_USER}
    Sleep    0.5
    Input Text    css=[itemid="cadastrarEmailUsuario"]    ${EMAIL_USER}
    Sleep    0.5
    Input Text    css=[itemid="cadastrarSenhaUsuario"]    ${PASSWORD_USER}
    Sleep    0.5
    Click Element    css=[itemid="cadastrarLocatarioUsuario"]    
    Sleep    0.5
    Click Button    css=[itemid="BtnCadastrarUsuario"]

Ver Sobre Usuario Locatario
    Sleep    1
    Wait Until Element Is Visible    css=[itemid="visibility-${NAME_USER}"]
    Click Element    css=[itemid="visibility-${NAME_USER}"]
    Sleep    1
    Click Button    css=[itemid="BtnSobreUsuario"]

Editar Usuario Locatario
    Sleep    1
    Wait Until Element Is Visible    css=[itemid="edit-${NAME_USER}"]    
    Click Element    css=[itemid="edit-${NAME_USER}"]
    Sleep    1
    Click Element    css=[itemid="editarNomeUsuario"]
    Sleep    0.5
    Press Keys       css=[itemid="editarNomeUsuario"]    CTRL+A+BACKSPACE
    Sleep    0.5
    Input Text    css=[itemid="editarNomeUsuario"]    ${NAME_USER_UPDATE}
    Sleep    0.5
    Click Element    css=[itemid="emailNomeUsuario"]
    Sleep    0.5
    Press Keys       css=[itemid="emailNomeUsuario"]    CTRL+A+BACKSPACE
    Sleep    0.5
    Input Text    css=[itemid="emailNomeUsuario"]    ${EMAIL_USER_UPDATE}
    Sleep    0.5
    Click Element    css=[itemid="editarLocatarioUsuario"]
    Sleep    0.5
    Click Button    css=[itemid="BtnEditarUsuario"]

Criar Usuario Admin
    Click Item In Menu    Usuário
    Sleep    0.5
    Wait Until Element Is Visible    css=[itemid="menu-item-home"]    timeout=10s
    Sleep    1
    Click Button    css=[itemid="cadastroBtnUsuario"]
    Sleep    0.5
    Input Text    css=[itemid="cadastroNomeUsuario"]    ${NAME_ADMIN}
    Sleep    0.5
    Input Text    css=[itemid="cadastrarEmailUsuario"]    ${EMAIL_ADMIN}
    Sleep    0.5
    Input Text    css=[itemid="cadastrarSenhaUsuario"]    ${PASSWORD_ADMIN}
    Sleep    0.5
    Click Element    css=[itemid="cadastrarAdministradorUsuario"]    
    Sleep    0.5
    Click Button    css=[itemid="BtnCadastrarUsuario"]

Ver Sobre Usuario Admin
    Sleep    1
    Wait Until Element Is Visible    css=[itemid="visibility-${NAME_ADMIN}"]
    Click Element    css=[itemid="visibility-${NAME_ADMIN}"]
    Sleep    1
    Click Button    css=[itemid="BtnSobreUsuario"]

Teste de Pesquisa Locatario
    Sleep    1
    Input Text    css=[itemid="searchInput"]    ${NAME_USER_UPDATE}
    Click Element    css=[itemid="searchBtn"]
    Sleep    2
    Click Element    css=[itemid="closeSearchBtn"]
    Sleep    1
    Input Text    css=[itemid="searchInput"]    ${EMAIL_USER_UPDATE}
    Click Element    css=[itemid="searchBtn"]
    Sleep    2
    Click Element    css=[itemid="closeSearchBtn"]
    Sleep    1
    Input Text    css=[itemid="searchInput"]    Locatario
    Click Element    css=[itemid="searchBtn"]
    Sleep    2
    Click Element    css=[itemid="closeSearchBtn"]

Teste de Pesquisa Admin
    Sleep    1
    Input Text    css=[itemid="searchInput"]    ${NAME_ADMIN}
    Click Element    css=[itemid="searchBtn"]
    Sleep    2
    Click Element    css=[itemid="closeSearchBtn"]
    Sleep    1
    Input Text    css=[itemid="searchInput"]    ${EMAIL_ADMIN}
    Click Element    css=[itemid="searchBtn"]
    Sleep    2
    Click Element    css=[itemid="closeSearchBtn"]
    Sleep    1
    Input Text    css=[itemid="searchInput"]    Administrador
    Click Element    css=[itemid="searchBtn"]
    Sleep    2
    Click Element    css=[itemid="closeSearchBtn"]

Close Browser
    Close All Browsers
