/**
 * An action to be taken on failure or success of a passing test
 */
export enum ActionType {
    ADD_TAGS = 'Add Tags',
    REMOVE_TAGS = 'Remove Tags',
    ADD_OWNERS = 'Add Owners',
    REMOVE_OWNERS = 'Remove Owners',
    ADD_GLOSSARY_TERMS = 'Add Glossary Terms',
    REMOVE_GLOSSARY_TERMS = 'Remove Glossary Terms',
    SET_DOMAIN = 'Set Domain',
    UNSET_DOMAIN = 'Unset Domain',
    PROPOSE_TAGS = 'Propose Tags',
    PROPOSE_TERMS = 'Propose Glossary Terms',
}

export type Action = {
    id: ActionType;
};

export type ResultActions = {
    onSuccess: Action[];
    onFailure: Action[];
};
