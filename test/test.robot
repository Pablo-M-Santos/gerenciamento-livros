*** Settings ***
Library           SeleniumLibrary
Library           Collections
Library           BuiltIn
Library           XML
Suite Setup       Iniciar Navegador
Suite Teardown    Fechar Navegador

*** Variables ***
${URL}             http://localhost:9000
${EMAIL}           admin@gmail.com
${PASSWORD}        12345678
${BROWSER}         firefox  # Alterado para Firefox (garante o uso correto da variável)

*** Test Cases ***
Usuario
    Login

*** Keywords ***

Iniciar Navegador
    ${firefox_options}=    Create Firefox Options  # Altere para Firefox
    Open Browser    ${URL}    ${BROWSER}    options=${firefox_options}  # Usar Firefox
    Maximize Browser Window
    Sleep    1

Fechar Navegador
    Close Browser

Create Firefox Options
    ${options}=    Evaluate    sys.modules['selenium.webdriver.firefox.options'].Options()    sys, selenium.webdriver.firefox.options
    Call Method    ${options}    add_argument    --headless
    Call Method    ${options}    add_argument    --no-sandbox
    Call Method    ${options}    add_argument    --disable-dev-shm-usage
    RETURN    ${options}

Login
    Log    Abrindo o navegador em ${URL}
    Wait Until Element Is Visible    css=[itemid="emailInput"]    timeout=10s
    Click Element    css=[itemid="emailInput"]
    Sleep    0.5

    Element Should Be Enabled    css=[itemid="emailInput"]
    Input Text    css=[itemid="emailInput"]    ${EMAIL}
    Sleep    0.5
    
    Wait Until Element Is Visible    css=[itemid="passwordInput"]    timeout=10s
    Click Element    css=[itemid="passwordInput"]
    Sleep    0.5

    Element Should Be Enabled    css=[itemid="passwordInput"]
    Input Text    css=[itemid="passwordInput"]    ${PASSWORD}
    Sleep    0.5

    Click Element    css=[itemid="loginBtn"]
    Sleep    2

Click Item In Menu
    [Arguments]    ${menu_item}
    ${itemid}=    Set Variable    menu-item-${menu_item.lower().replace(" ", "-")}
    Click Element    css=[itemid=${itemid}]
