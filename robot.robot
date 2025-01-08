*** Settings ***
Library    SeleniumLibrary

*** Variables ***
${URL}    http://localhost:9000

*** Test Cases ***
Open Localhost
    Open Browser    ${URL}    Chrome
    Sleep    3s
    Close Browser
