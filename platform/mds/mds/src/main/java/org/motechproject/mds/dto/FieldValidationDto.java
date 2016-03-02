package org.motechproject.mds.dto;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.LinkedList;
import java.util.List;

/**
 * The <code>FieldValidationDto</code> class contains information about validation criteria
 * for field.
 */
public class FieldValidationDto {
    private List<ValidationCriterionDto> criteria = new LinkedList<>();

    /**
     * Constant <code>INTEGER</code> contains validation criteria for integer type.
     */
    public static final FieldValidationDto INTEGER = new FieldValidationDto(
            new ValidationCriterionDto("mds.field.validation.minValue", TypeDto.INTEGER),
            new ValidationCriterionDto("mds.field.validation.maxValue", TypeDto.INTEGER),
            new ValidationCriterionDto("mds.field.validation.mustBeInSet", TypeDto.STRING),
            new ValidationCriterionDto("mds.field.validation.cannotBeInSet", TypeDto.STRING)
    );

    /**
     * Constant <code>DOUBLE</code> contains validation criteria for double type.
     */
    public static final FieldValidationDto DOUBLE = new FieldValidationDto(
            new ValidationCriterionDto("mds.field.validation.minValue", TypeDto.DOUBLE),
            new ValidationCriterionDto("mds.field.validation.maxValue", TypeDto.DOUBLE),
            new ValidationCriterionDto("mds.field.validation.mustBeInSet", TypeDto.STRING),
            new ValidationCriterionDto("mds.field.validation.cannotBeInSet", TypeDto.STRING)
    );

    /**
     * Constant <code>FLOAT</code> contains validation criteria for float type.
     */
    public static final FieldValidationDto FLOAT = new FieldValidationDto(
            new ValidationCriterionDto("mds.field.validation.minValue", TypeDto.FLOAT),
            new ValidationCriterionDto("mds.field.validation.maxValue", TypeDto.FLOAT),
            new ValidationCriterionDto("mds.field.validation.mustBeInSet", TypeDto.STRING),
            new ValidationCriterionDto("mds.field.validation.cannotBeInSet", TypeDto.STRING)
    );

    /**
     * Constant <code>STRING</code> contains validation criteria for string type.
     */
    public static final FieldValidationDto STRING = new FieldValidationDto(
            new ValidationCriterionDto("mds.field.validation.regex", TypeDto.STRING),
            new ValidationCriterionDto("mds.field.validation.minLength", TypeDto.INTEGER),
            new ValidationCriterionDto("mds.field.validation.maxLength", TypeDto.INTEGER)
    );

    public FieldValidationDto() {
    }

    public FieldValidationDto(ValidationCriterionDto... criteria) {
        if (criteria != null) {
            for (ValidationCriterionDto criterion : criteria) {
                this.criteria.add(new ValidationCriterionDto(criterion.getDisplayName(), criterion.getType(),
                        criterion.getValue(), criterion.isEnabled()));
            }
        }
    }

    public void addCriterion(ValidationCriterionDto criterion) {
        criteria.add(criterion);
    }

    @JsonIgnore
    public ValidationCriterionDto getCriterion(String displayName) {
        ValidationCriterionDto found = null;

        for (ValidationCriterionDto criterion : criteria) {
            if (criterion.getDisplayName().equalsIgnoreCase(displayName)) {
                found = criterion;
                break;
            }
        }

        return found;
    }

    public List<ValidationCriterionDto> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<ValidationCriterionDto> criteria) {
        this.criteria = criteria;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
