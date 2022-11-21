describe("glossary", () => {
    it("go to glossary page, create terms, term group", () => {

        const urn = "urn:li:dataset:(urn:li:dataPlatform:hive,cypress_logging_events,PROD)";
        const datasetName = "cypress_logging_events";
        const glossaryTerm = "CypressGlosssaryTerm";
        const glossaryTermGroup = "CypressGlosssaryGroup";
        cy.login();
        cy.goToGlossaryList();

        cy.clickOptionWithText("Add Term");
        cy.addViaModelTestId(glossaryTerm, "Create Glossary Term", "add");
        cy.waitTextVisible("Created Glossary Term!")

        cy.clickOptionWithText("Add Term Group");
        cy.addViaModelTestId(glossaryTermGroup, "Create Term Group", "add");
        cy.waitTextVisible("Created Term Group!")

        cy.addTermToDataset(urn, datasetName, glossaryTerm);

        cy.goToGlossaryList();
        cy.clickOptionWithText(glossaryTerm);
        cy.deleteFromDropdown();
        cy.waitTextVisible("Deleted Glossary Term!")

        cy.goToDataset(urn, datasetName);
        cy.ensureTextNotPresent(glossaryTerm);

        cy.goToGlossaryList();
        cy.clickOptionWithText(glossaryTermGroup);
        cy.deleteFromDropdown();
        cy.waitTextVisible("Deleted Term Group!")

        cy.goToGlossaryList();
        cy.ensureTextNotPresent(glossaryTermGroup);
    });
});
