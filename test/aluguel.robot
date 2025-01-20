*** Settings ***
Library            Collections
Library            BuiltIn
Library            SeleniumLibrary
Library            XML
Suite Setup        Iniciar Navegador
Suite Teardown     Fechar Navegador


*** Variables ***
${BROWSER}                 firefox
${URL}                     https://locadora-pablo.altislabtech.com.br/
${BASE_URL}                www.google.com
${HEADLESS_OPTIONS}        ${EMPTY}
${EMAIL}                   admin@gmail.com
${PASSWORD}                12345678
${DATA_RENT}               22/11
${DATA_RENT_UPDATE}        21/12
${NAME_ALUGUEL}            Locatario



*** Test Cases ***
Aluguel
    Login
    Cadastrar Aluguel
    # Editar Aluguel
    # Teste de pesquisa
    # Teste de Entrega


*** Keywords ***

Login
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



Cadastrar Aluguel

    Click Item In Menu    Aluguel

    Wait Until Element Is Visible    css=[itemid="menu-item-home"]    timeout=10s

    Sleep    1

    Click Button    css=[itemid="cadastroBtnAluguel"]

    Wait Until Page Contains Element    //div[@class='q-field__control-container col relative-position row no-wrap q-anchor--skip'][contains(.,'Selecione o Locatário')]    timeout=10s
    Click Element    //div[@class='q-field__control-container col relative-position row no-wrap q-anchor--skip'][contains(.,'Selecione o Locatário')]

    Wait Until Page Contains Element    //div[@role="option" and .//span[text()="LOCATARIO"]]    timeout=10s
    Click Element    //div[@role="option" and .//span[text()="LOCATARIO"]]

    Wait Until Page Contains Element    //div[@class='q-field__control-container col relative-position row no-wrap q-anchor--skip'][contains(.,'Selecione o Livro')]    timeout=10s
    Click Element    //div[@class='q-field__control-container col relative-position row no-wrap q-anchor--skip'][contains(.,'Selecione o Livro')]

    Wait Until Page Contains Element    //div[@role="option" and .//span[text()="Livro Robot"]]    timeout=10s
    Click Element    //div[@role="option" and .//span[text()="Livro Robot"]]

    Capture Page Screenshot

    Execute JavaScript    var el = document.querySelector('[itemid="cadastrarDataAluguel"]'); el.value = "2025-01-22"; el.dispatchEvent(new Event('input')); el.dispatchEvent(new Event('change'));
    ${data_valor}=    Execute JavaScript    return document.querySelector('[itemid="cadastrarDataAluguel"]').value;
    Should Be Equal As Strings    ${data_valor}    2025-01-22

    Click Button    css=[itemid="BtnCadastrarAluguel"]

    Capture Page Screenshot



Editar Aluguel
    Sleep    1

    Wait Until Element Is Visible    css=[itemid="edit-1"]
    Click Element    css=[itemid="edit-1"]

    Sleep    1

    Wait Until Page Contains Element    //div[@class='q-field__control-container col relative-position row no-wrap q-anchor--skip'][contains(.,'Selecione o Locatário')]    timeout=10s
    Click Element    //div[@class='q-field__control-container col relative-position row no-wrap q-anchor--skip'][contains(.,'Selecione o Locatário')]

    Wait Until Page Contains Element    //div[@role="option" and .//span[text()="Locatario2"]]    timeout=10s
    Click Element    //div[@role="option" and .//span[text()="Locatario2"]]

    Wait Until Page Contains Element    //div[@class='q-field__control-container col relative-position row no-wrap q-anchor--skip'][contains(.,'Selecione o Livro')]    timeout=10s
    Click Element    //div[@class='q-field__control-container col relative-position row no-wrap q-anchor--skip'][contains(.,'Selecione o Livro')]

    Wait Until Page Contains Element    //div[@role="option" and .//span[text()="Livro 2"]]    timeout=10s
    Click Element    //div[@role="option" and .//span[text()="Livro 2"]]

    Input Text    css=[itemid="editarDataAluguel"]    ${DATA_RENT_UPDATE}

    Click Button    css=[itemid="BtnEditarAluguel"]



Teste de pesquisa

    Sleep    1

    Input Text    css=[itemid="searchInput"]    ${NAME_ALUGUEL}

    Press Keys       css=[itemid="searchInput"]    ENTER

    Sleep    1

    Press Keys       css=[itemid="searchInput"]    CTRL+A+BACKSPACE

    Press Keys       css=[itemid="searchInput"]    ENTER

    Sleep    1


Teste de Entrega    
    Sleep    1

    Wait Until Element Is Visible    css=[itemid="confirmar-1"]
    Click Element    css=[itemid="confirmar-1"]

    Click Button    css=[itemid="BtnEntregaAluguel"]
    
Fechar Navegador
    Close Browser

Iniciar Navegador
    ${options}=    Evaluate    sys.modules['selenium.webdriver'].FirefoxOptions()    sys, selenium.webdriver
    Call Method    ${options}    add_argument    --headless
    Open Browser    ${URL}    ${BROWSER}    options=${options}
    Set Selenium Speed    3s

Create Chrome Options
    ${options}=    Evaluate    sys.modules['selenium.webdriver.chrome.options'].Options()    sys, selenium.webdriver.chrome.options
    Call Method    ${options}    add_argument    --headless
    # Call Method    ${options}    add_argument    --no-sandbox
    # Call Method    ${options}    add_argument    --disable-software-rasterizer
    # Call Method    ${options}    add_argument    --disable-extensions
    # Call Method    ${options}    add_argument    --disable-dev-shm-usage
    # Call Method    ${options}    add_argument    --disable-gpu  # Adicionado para evitar possÃveis falhas de GPU no ambiente headless
    RETURN    ${options}