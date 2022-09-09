import { ActionsStep } from './steps/actions/ActionsStep';
import { NameStep } from './steps/name/NameStep';
import { RulesStep } from './steps/rules/RulesStep';
import { SelectStep } from './steps/select/SelectStep';

/**
 * Mapping from the step type to the component implementing that step.
 */
export const TestBuilderStepComponent = {
    SELECT: SelectStep,
    RULES: RulesStep,
    ACTIONS: ActionsStep,
    NAME: NameStep,
};

/**
 * Mapping from the step type to the title for the step
 */
export enum TestBuilderStepTitles {
    SELECT = 'Select',
    RULES = 'Rules',
    ACTIONS = 'Actions',
    NAME = 'Finish up',
}
