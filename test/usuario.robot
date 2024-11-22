*** Settings ***
Library           RequestsLibrary
Library           Collections
Library           BuiltIn
Library           SeleniumLibrary
Library            XML
Resource    autentificacao.robot

*** Variables ***
${BASE_URL}        http://localhost:9000
${EMAIL}           admin@gmail.com
${PASSWORD}        12345678
${NAME_USER}       Test Robot
${EMAIL_USER}      testRobot1@gmail.com
${PASSWORD_USER}   12345678
${NAME_USER_UPDATE}       Test Robot Atualizacao
${EMAIL_USER_UPDATE}      testRobotAtualizacao@gmail.com
    



*** Test Cases ***
Usuario
    Login
    Cadastrar Usuario
    Editar Usuario
    Teste de filtro de usuários
    Teste de pesquisa
    Teste Sobre Usuário



*** Keywords ***

Login
    Open Browser    ${BASE_URL}    chrome
    Maximize Browser Window
    Wait Until Element Is Visible    css=[itemid="emailInput"]    timeout=10s
    Click Element    css=[itemid="emailInput"]

    Element Should Be Enabled        css=[itemid="emailInput"]
    Input Text    css=[itemid="emailInput"]    ${EMAIL}

    Wait Until Element Is Visible    css=[itemid="passwordInput"]    timeout=10s
    Click Element    css=[itemid="passwordInput"]

    Element Should Be Enabled        css=[itemid="passwordInput"]
    Input Text    css=[itemid="passwordInput"]    ${PASSWORD}

    Click Element    css=[itemid="loginBtn"]
    Sleep    2

Click Item In Menu
    [Arguments]    ${menu_item}
    ${itemid}=    Set Variable    menu-item-${menu_item.lower().replace(" ", "-")}
    Click Element    css=[itemid=${itemid}]



Cadastrar Usuario

    Click Item In Menu    Usuário

    Wait Until Element Is Visible    css=[itemid="menu-item-home"]    timeout=10s

    Sleep    1

    Click Button    css=[itemid="cadastroBtnUsuario"]

    Input Text    css=[itemid="cadastroNomeUsuario"]    ${NAME_USER}

    Input Text    css=[itemid="cadastrarEmailUsuario"]    ${EMAIL_USER}

    Input Text    css=[itemid="cadastrarSenhaUsuario"]    ${PASSWORD_USER}

    Click Element    css=[itemid="cadastrarAdministradorUsuario"]    

    Click Button    css=[itemid="BtnCadastrarUsuario"]

Editar Usuario


    Sleep    1

    Wait Until Element Is Visible    css=[itemid="edit-Test Robot"]    
    Click Element    css=[itemid="edit-Test Robot"]
    
    Sleep    1

    Click Element    css=[itemid="editarNomeUsuario"]

    Press Keys       css=[itemid="editarNomeUsuario"]    CTRL+A+BACKSPACE

    Input Text    css=[itemid="editarNomeUsuario"]    ${NAME_USER_UPDATE}

    Click Element    css=[itemid="emailNomeUsuario"]

    Press Keys       css=[itemid="emailNomeUsuario"]    CTRL+A+BACKSPACE

    Input Text    css=[itemid="emailNomeUsuario"]    ${EMAIL_USER_UPDATE}

    Click Element    css=[itemid="editarLocatarioUsuario"]    

    Click Button    css=[itemid="BtnEditarUsuario"]


Teste de filtro de usuários

    Sleep    1

    Click Element    css=[itemid="filterBtn"]
    Sleep    0.5

    Click Element    css=[itemid="filterEditorBtn"]
    Sleep    2

    Click Element    css=[itemid="filterBtn"]
    Sleep    0.5

    Click Element    css=[itemid="filterLeitorBtn"]
    Sleep    2

    Click Element    css=[itemid="filterBtn"]
    Sleep    0.5

    Click Element    css=[itemid="filterTodosBtn"]
    Sleep    2

    Close Browser

Teste de pesquisa

    Sleep    1

    Input Text    css=[itemid="searchInput"]    ${NAME_USER_UPDATE}

    Click Element    css=[itemid="searchBtn"]

    Sleep    2

    Click Element    css=[itemid="closeSearchBtn"]

    Sleep    1


Teste Sobre Usuário

    Sleep    1   

    Wait Until Element Is Visible    css=[itemid="visibility-Test Robot Atualizacao"]    
    Click Element    css=[itemid="visibility-Test Robot Atualizacao"]
    
    Sleep    1

    Click Button    css=[itemid="BtnSobreUsuario"]