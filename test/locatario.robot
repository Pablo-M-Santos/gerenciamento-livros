*** Settings ***
Library            Collections
Library            BuiltIn
Library            SeleniumLibrary
Library            XML
Suite Setup        Iniciar Navegador
Suite Teardown     Fechar Navegador

*** Variables ***
${BROWSER}                     firefox
${URL}                         https://locadora-pablo.altislabtech.com.br/
${BASE_URL}                    www.google.com
${HEADLESS_OPTIONS}            ${EMPTY}
${EMAIL}                       admin@gmail.com
${PASSWORD}                    12345678
${NAME_RENTER}                 Locatario Robot
${EMAIL_RENTER}                locatarioRobot@gmail.com
${TELEPHONE_RENTER}            85987858860
${ADRESS_RENTER}               wda
${CPF_RENTER}                  08214996104
${NAME_RENTER_UPDATE}          locatario Robot Atualizacao
${EMAIL_RENTER_UPDATE}         locatarioRobotAtualizacao@gmail.com
${TELEPHONE_RENTER_UPDATE}     85987858861
${ADRESS_RENTER_UPDATE}        wdaa
${CPF_RENTER_UPDATE}           01833442199




*** Test Cases ***
Locatario
    Login
    Cadastrar Locatario
    Editar Locatario
    Teste de pesquisa
    Teste Sobre Locatario
    Teste Exclusão Locatario


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



Cadastrar Locatario

    Click Item In Menu    Locatário

    Wait Until Element Is Visible    css=[itemid="menu-item-home"]    timeout=10s

    Sleep    1

    Click Button    css=[itemid="cadastroBtnLocatario"]

    Sleep    1

    Input Text    css=[itemid="cadastroNomeLocatario"]    ${NAME_RENTER}

    Sleep    1
    
    Input Text    css=[itemid="cadastrarEmailLocatario"]    ${EMAIL_RENTER}

    Sleep    1

    Input Text    css=[itemid="cadastroTelefoneLocatario"]    ${TELEPHONE_RENTER}

    Sleep    1

    Input Text    css=[itemid="cadastroEnderecoLocatario"]    ${ADRESS_RENTER}

    Input Text    css=[itemid="cadastrarCPFLocatario"]    ${CPF_RENTER}

    Click Button    css=[itemid="BtnCadastrarLocatario"]

    Sleep    1

Editar Locatario
    Sleep    1

    Wait Until Element Is Visible    css=[itemid="edit-Locatario Robot"]    
    Click Element    css=[itemid="edit-Locatario Robot"]
    
    Sleep    1

    Click Element    css=[itemid="editarNomeLocatario"]

    Press Keys       css=[itemid="editarNomeLocatario"]    CTRL+A+BACKSPACE

    Input Text    css=[itemid="editarNomeLocatario"]    ${NAME_RENTER_UPDATE}

    Click Element    css=[itemid="editarEmailLocatario"]

    Press Keys       css=[itemid="editarEmailLocatario"]    CTRL+A+BACKSPACE

    Input Text    css=[itemid="editarEmailLocatario"]    ${EMAIL_RENTER_UPDATE}

    Click Element    css=[itemid="editarTelefoneLocatario"]

    Press Keys       css=[itemid="editarTelefoneLocatario"]    CTRL+A+BACKSPACE

    Input Text    css=[itemid="editarTelefoneLocatario"]    ${TELEPHONE_RENTER_UPDATE}

    Click Element    css=[itemid="editarEnderecoLocatario"]

    Press Keys       css=[itemid="editarEnderecoLocatario"]    CTRL+A+BACKSPACE

    Input Text    css=[itemid="editarEnderecoLocatario"]    ${ADRESS_RENTER_UPDATE}

    Click Element    css=[itemid="editarCPFLocatario"]

    Press Keys       css=[itemid="editarCPFLocatario"]    CTRL+A+BACKSPACE

    Input Text    css=[itemid="editarCPFLocatario"]    ${CPF_RENTER_UPDATE}

    Click Button    css=[itemid="BtnEditarLocatario"]



Teste de pesquisa

    Sleep    1

    Input Text    css=[itemid="searchInput"]    ${NAME_RENTER_UPDATE}

    Press Keys       css=[itemid="searchInput"]    ENTER

    Press Keys       css=[itemid="searchInput"]    CTRL+A+BACKSPACE

    Press Keys       css=[itemid="searchInput"]    ENTER

    Sleep    1

    Input Text    css=[itemid="searchInput"]    ${EMAIL_RENTER_UPDATE}

    Press Keys       css=[itemid="searchInput"]    ENTER

    Press Keys       css=[itemid="searchInput"]    CTRL+A+BACKSPACE

    Press Keys       css=[itemid="searchInput"]    ENTER

    Sleep    1

    Input Text    css=[itemid="searchInput"]    ${TELEPHONE_RENTER_UPDATE}

    Press Keys       css=[itemid="searchInput"]    ENTER

    Press Keys       css=[itemid="searchInput"]    CTRL+A+BACKSPACE

    Press Keys       css=[itemid="searchInput"]    ENTER

    Sleep    1


Teste Sobre Locatario

    Sleep    1   

    Wait Until Element Is Visible    css=[itemid="visibility-locatario Robot Atualizacao"]    
    Click Element    css=[itemid="visibility-locatario Robot Atualizacao"]
    
    Sleep    1

    Click Button    css=[itemid="BtnSobreLocatario"]

Teste Exclusão Locatario

    Sleep    1   

    Wait Until Element Is Visible    css=[itemid="delete-locatario Robot Atualizacao"]    
    Click Element    css=[itemid="delete-locatario Robot Atualizacao"]
    
    Sleep    1

    Click Button    css=[itemid="BtnExcluirLocatario"]


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