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

${TITLE_BOOK}       Livro Robot
${AUTOR_BOOK}       Autor Robot
${QUANTIDADE_BOOK}     10
${DATE_BOOK}       22/11/2024
${TITLE_BOOK_UPDATE}       Livro Robot Atualizacao
${AUTOR_BOOK_UPDATE}       Autor Robot Atualizacao
${QUANTIDADE_BOOK_UPDATE}     11
${DATE_BOOK_UPDATE}       21/11/2024





*** Test Cases ***
Livro
    Login
    Cadastrar Livro
    Editar Livro
    Teste de pesquisa
    Teste Sobre Livro
    Teste Exclusão Livro



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



Cadastrar Livro

    Click Item In Menu    Livros

    Wait Until Element Is Visible    css=[itemid="menu-item-home"]    timeout=10s

    Sleep    1

    Click Button    css=[itemid="cadastroBtnLivro"]

    Input Text    css=[itemid="cadastroTituloLivro"]    ${TITLE_BOOK}
    
    Input Text    css=[itemid="cadastroAutorLivro"]    ${AUTOR_BOOK}

    Input Text    css=[itemid="cadastrarQuantidadeLivro"]    ${QUANTIDADE_BOOK}

    Input Text    css=[itemid="cadastrarDataLivro"]    ${DATE_BOOK}

    Wait Until Page Contains Element    //div[@class='q-field__control-container col relative-position row no-wrap q-anchor--skip'][contains(.,'Selecione a editora')]    timeout=10s
    Click Element    //div[@class='q-field__control-container col relative-position row no-wrap q-anchor--skip'][contains(.,'Selecione a editora')]

    Wait Until Page Contains Element    //div[@role="option" and .//span[text()="EDITORA"]]    timeout=10s
    Click Element    //div[@role="option" and .//span[text()="EDITORA"]]
    

    Click Button    css=[itemid="BtnCadastrarLivro"]

Editar Livro
    Sleep    1

    Wait Until Element Is Visible    css=[itemid="edit-Livro Robot"]    
    Click Element    css=[itemid="edit-Livro Robot"]
    
    Sleep    1

    Click Element    css=[itemid="editarTituloLivro"]

    Press Keys       css=[itemid="editarTituloLivro"]    CTRL+A+BACKSPACE

    Input Text    css=[itemid="editarTituloLivro"]    ${TITLE_BOOK_UPDATE}



    Click Element    css=[itemid="editarAutorLivro"]

    Press Keys       css=[itemid="editarAutorLivro"]    CTRL+A+BACKSPACE

    Input Text    css=[itemid="editarAutorLivro"]    ${AUTOR_BOOK_UPDATE}



    Click Element    css=[itemid="editarQuantidadeLivro"]

    Press Keys       css=[itemid="editarQuantidadeLivro"]    CTRL+A+BACKSPACE

    Input Text    css=[itemid="editarQuantidadeLivro"]    ${QUANTIDADE_BOOK_UPDATE}


    Click Element    css=[itemid="editarDataLivro"]

    Press Keys       css=[itemid="editarDataLivro"]    CTRL+A+BACKSPACE

    Input Text    css=[itemid="editarDataLivro"]    ${DATE_BOOK_UPDATE}

    Wait Until Page Contains Element    //div[@class='q-field__control-container col relative-position row no-wrap q-anchor--skip'][contains(.,'Selecione a editora')]    timeout=10s
    Click Element    //div[@class='q-field__control-container col relative-position row no-wrap q-anchor--skip'][contains(.,'Selecione a editora')]

    Wait Until Page Contains Element    //div[@role="option" and .//span[text()="EDITORA2"]]    timeout=10s
    Click Element    //div[@role="option" and .//span[text()="EDITORA2"]]

    Click Button    css=[itemid="BtnEditarLivro"]



Teste de pesquisa

    Sleep    1

    Input Text    css=[itemid="searchInput"]    ${TITLE_BOOK_UPDATE}

    Press Keys       css=[itemid="searchInput"]    ENTER

    Sleep    1

    Press Keys       css=[itemid="searchInput"]    CTRL+A+BACKSPACE

    Press Keys       css=[itemid="searchInput"]    ENTER

    Sleep    1


Teste Sobre Livro

    Sleep    1   

    Wait Until Element Is Visible    css=[itemid="visibility-Livro Robot Atualizacao"]    
    Click Element    css=[itemid="visibility-Livro Robot Atualizacao"]
    
    Sleep    1

    Click Button    css=[itemid="BtnSobreLivro"]

Teste Exclusão Livro

    Sleep    1   

    Wait Until Element Is Visible    css=[itemid="delete-Livro Robot Atualizacao"]    
    Click Element    css=[itemid="delete-Livro Robot Atualizacao"]
    
    Sleep    1

    Click Button    css=[itemid="BtnExcluirLivro"]