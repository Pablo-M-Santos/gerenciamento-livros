*** Settings ***
Library           RequestsLibrary
Library           Collections
Library           BuiltIn
Library           SeleniumLibrary
Library           XML
Suite Setup       Iniciar Navegador
Suite Teardown    Fechar Navegador

*** Variables ***
${URL}                http://localhost:9000
${EMAIL}                   admin@gmail.com
${PASSWORD}                12345678
${BROWSER}                 Chrome

*** Test Cases ***
Usuario
    Login


*** Keywords ***

Iniciar Navegador
    ${chrome_options}=    Create Chrome Options
    Open Browser    ${URL}    chrome    options=${chrome_options}
Fechar Navegador
    Close Browser

Create Chrome Options
    ${options}=    Evaluate    sys.modules['selenium.webdriver.chrome.options'].Options()    sys, selenium.webdriver.chrome.options
    Call Method    ${options}    add_argument    --headless
    Call Method    ${options}    add_argument    --no-sandbox
    Call Method    ${options}    add_argument    --disable-dev-shm-usage
    RETURN    ${options}

Login
    Open Browser  http://localhost:9000  ${BROWSER}  
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



Close Browser
    Close All Browsers
