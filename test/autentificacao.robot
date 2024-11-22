*** Settings ***
Library           RequestsLibrary
Library           Collections
Library           BuiltIn
Library           SeleniumLibrary
Library            XML

*** Variables ***
${BASE_URL}    http://localhost:9000
${EMAIL}       admin@gmail.com
${PASSWORD}    12345678



*** Test Cases ***
Login
    Login
    Teste de Sidebar
    Teste de Logout


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
    Log    ${itemid}  # Para depuração, verifique o ID gerado
    Click Element    css=[itemid=${itemid}]

Teste de Sidebar
    Click Item In Menu    Home
    Sleep    1
    Click Item In Menu    Editora
    Sleep    1
    Click Item In Menu    Livros
    Sleep    1
    Click Item In Menu    Locatário
    Sleep    1
    Click Item In Menu    Aluguel
    Sleep    1
    Click Item In Menu    Usuário
    Sleep    1

Teste de Logout
    Wait Until Element Is Visible    css=[itemid="logoutBtn"]    timeout=10s
    Click Element    css=[itemid="logoutBtn"]

    Sleep    2

    Close Browser