*** Settings ***
Library           RequestsLibrary
Library           Collections
Library           BuiltIn
Library           SeleniumLibrary
Library            XML


*** Variables ***
${BASE_URL}        http://localhost:9000
${EMAIL}           admin@gmail.com
${PASSWORD}        12345678

${NAME_PUBLISHER}               Editora Robot
${TELEPHONE_PUBLISHER}          85987858860
${EMAIL_PUBLISHER}              editoraRobot@gmail.com
${SITE_PUBLISHER}               https://wda.com.br
${NAME_PUBLISHER_UPDATE}        Editora Robot Atualizacao
${TELEPHONE_PUBLISHER_UPDATE}   85987858861
${EMAIL_PUBLISHER_UPDATE}       editoraRobotAtualizacao@gmail.com
${SITE_PUBLISHER_UPDATE}        https://wda1.com.br

*** Test Cases ***
Editora
    Login
    Cadastrar Editora
    Teste Sobre Editora
    Editar Editora
    Teste de Pesquisa por Nome
    Teste Exclusão Editora

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
    Sleep    0.5

Click Item In Menu
    [Arguments]    ${menu_item}
    ${itemid}=    Set Variable    menu-item-${menu_item.lower().replace(" ", "-")}
    Click Element    css=[itemid=${itemid}]
    Sleep    2

Cadastrar Editora
    Click Item In Menu    Editora
    Wait Until Element Is Visible    css=[itemid="menu-item-home"]    timeout=10s
    Sleep    0.5
    Click Button    css=[itemid="cadastroBtnEditora"]
    Input Text    css=[itemid="cadastroNomeEditora"]    ${NAME_PUBLISHER}
    Sleep    0.5
    Input Text    css=[itemid="cadastroTelefoneEditora"]    ${TELEPHONE_PUBLISHER}
    Sleep    0.5
    Input Text    css=[itemid="cadastrarEmailEditora"]    ${EMAIL_PUBLISHER}
    Sleep    0.5
    Input Text    css=[itemid="cadastrarSiteEditora"]    ${SITE_PUBLISHER}
    Sleep    0.5
    Click Button    css=[itemid="BtnCadastrarEditora"]
    Sleep    1

Editar Editora
    Sleep    0.5
    Wait Until Element Is Visible    css=[itemid="edit-Editora Robot"]
    Click Element    css=[itemid="edit-Editora Robot"]
    Sleep    0.5
    Click Element    css=[itemid="editarNomeEditora"]
    Press Keys       css=[itemid="editarNomeEditora"]    CTRL+A+BACKSPACE
    Input Text    css=[itemid="editarNomeEditora"]    ${NAME_PUBLISHER_UPDATE}
    Sleep    0.5
    Click Element    css=[itemid="editarTelefoneEditora"]
    Press Keys       css=[itemid="editarTelefoneEditora"]    CTRL+A+BACKSPACE
    Input Text    css=[itemid="editarTelefoneEditora"]    ${TELEPHONE_PUBLISHER_UPDATE}
    Sleep    0.5
    Click Element    css=[itemid="editarEmailEditora"]
    Press Keys       css=[itemid="editarEmailEditora"]    CTRL+A+BACKSPACE
    Input Text    css=[itemid="editarEmailEditora"]    ${EMAIL_PUBLISHER_UPDATE}
    Sleep    0.5
    Click Element    css=[itemid="editarSiteEditora"]
    Press Keys       css=[itemid="editarSiteEditora"]    CTRL+A+BACKSPACE
    Input Text    css=[itemid="editarSiteEditora"]    ${SITE_PUBLISHER_UPDATE}
    Sleep    0.5
    Click Button    css=[itemid="BtnEditarEditora"]
    Sleep    1

Teste de Pesquisa por Nome
    Sleep    0.5
    Input Text    css=[itemid="searchInput"]    ${NAME_PUBLISHER_UPDATE}
    Sleep    0.5
    Press Keys       css=[itemid="searchInput"]    ENTER
    Sleep    0.5
    Press Keys       css=[itemid="searchInput"]    CTRL+A+BACKSPACE
    Sleep    0.5
    Press Keys       css=[itemid="searchInput"]    ENTER

    Input Text    css=[itemid="searchInput"]    ${EMAIL_PUBLISHER_UPDATE}
    Sleep    0.5

    Press Keys       css=[itemid="searchInput"]    ENTER
    Sleep    0.5

    Press Keys       css=[itemid="searchInput"]    CTRL+A+BACKSPACE
    Sleep    0.5
    Press Keys       css=[itemid="searchInput"]    ENTER

    Input Text    css=[itemid="searchInput"]    ${TELEPHONE_PUBLISHER_UPDATE}
    Sleep    0.5

    Press Keys       css=[itemid="searchInput"]    ENTER
    Sleep    0.5

    Press Keys       css=[itemid="searchInput"]    CTRL+A+BACKSPACE
    Sleep    0.5
    Press Keys       css=[itemid="searchInput"]    ENTER


Teste Sobre Editora
    Sleep    0.5
    Wait Until Element Is Visible    css=[itemid="visibility-Editora Robot"]
    Click Element    css=[itemid="visibility-Editora Robot"]
    Sleep    0.5
    Click Button    css=[itemid="BtnSobreUsuario"]
    Sleep    0.5

Teste Exclusão Editora
    Sleep    0.5
    Wait Until Element Is Visible    css=[itemid="delete-Editora Robot Atualizacao"]
    Click Element    css=[itemid="delete-Editora Robot Atualizacao"]
    Sleep    0.5
    Click Button    css=[itemid="BtnExcluirUsuario"]
    Sleep    0.5
