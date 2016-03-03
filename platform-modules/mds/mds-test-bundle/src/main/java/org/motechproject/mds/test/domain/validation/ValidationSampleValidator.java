package org.motechproject.mds.test.domain.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidationSampleValidator implements ConstraintValidator<BothNamesOrNoName, ValidationSample> {

    @Override
    public void initialize(BothNamesOrNoName bothNamesOrNoName) {

    }

    @Override
    public boolean isValid(ValidationSample validationSample, ConstraintValidatorContext constraintValidatorContext) {
        if (validationSample == null) {
            return true;
        }

        // either both have to be null or both set
        if(!(validationSample.getFirstName() == null ^ validationSample.getLastName() == null)) {
            return true;
        } else {
            constraintValidatorContext
                    .buildConstraintViolationWithTemplate(constraintValidatorContext.getDefaultConstraintMessageTemplate())
                    .addNode("firstName").addConstraintViolation();
            return false;
        }
    }
}
