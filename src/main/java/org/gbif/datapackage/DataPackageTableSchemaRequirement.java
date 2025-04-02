/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.datapackage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents what {@link DataPackageTableSchema} are required in the data package.
 */
@Data
public class DataPackageTableSchemaRequirement {

  private String description;
  private List<String> applicableIfPresentAny = new ArrayList<>();
  private List<DataPackageTableSchemaRequirement> allOf = new ArrayList<>();
  private List<DataPackageTableSchemaRequirement> anyOf = new ArrayList<>();
  private List<DataPackageTableSchemaRequirement> oneOf = new ArrayList<>();
  private List<String> required = new ArrayList<>();
  private List<String> requiredAny = new ArrayList<>();
  private List<String> prohibited = new ArrayList<>();

  public ValidationResult validate(Set<String> schemas) {
    ValidationResult result = new ValidationResult();

    // all conditions are empty, skip validation?
    if (allOf.isEmpty() && anyOf.isEmpty() && oneOf.isEmpty() && required.isEmpty() && requiredAny.isEmpty() && prohibited.isEmpty()) {
      result.setValid(true);
      return result;
    }

    // check validation applicable
    Collection<String> applicableSchemas = CollectionUtils.intersection(schemas, applicableIfPresentAny);
    if (!applicableIfPresentAny.isEmpty() && applicableSchemas.isEmpty()) {
      return result;
    }

    // check simple conditions first
    // if prohibited present - then validation fails
    if (!prohibited.isEmpty()) {
      Collection<String> intersection = CollectionUtils.intersection(schemas, prohibited);
      if (!intersection.isEmpty()) {
        result.setValid(false);
        result.setReason(String.format("Prohibited schemas found: %s. The following schemas are prohibited: %s", intersection, prohibited));
        return result;
      }
    }

    if (!required.isEmpty()) {
      boolean allRequiredSchemasPresent = CollectionUtils.containsAll(schemas, required);
      if (!allRequiredSchemasPresent) {
        result.setValid(false);
        result.setReason(String.format("All required schemas must be present: %s", required));
        return result;
      }
    }

    if (!requiredAny.isEmpty()) {
      Collection<String> intersection = CollectionUtils.intersection(schemas, requiredAny);
      if (intersection.isEmpty()) {
        result.setValid(false);
        result.setReason(String.format("At least one of required schemas must be present: %s", requiredAny));
        return result;
      }
    }

    // check complex conditions
    if (!allOf.isEmpty()) {
      for (DataPackageTableSchemaRequirement subRequirement : allOf) {
        ValidationResult subResult = subRequirement.validate(schemas);
        if (!subResult.isValid()) {
          result.setValid(false);
          result.setReason(subResult.getReason());
        }
      }
    } else if (!anyOf.isEmpty()) {
      result.setValid(false);
      result.setReason(String.format("At least one valid required, none found: %s", anyOf));
      for (DataPackageTableSchemaRequirement subRequirement : anyOf) {
        ValidationResult subResult = subRequirement.validate(schemas);
        if (subResult.isValid()) {
          result.setValid(true);
          result.setReason("");
          break;
        }
      }
    } else if (!oneOf.isEmpty()) {
      int numberOfValid = 0;
      for (DataPackageTableSchemaRequirement subRequirement : oneOf) {
        ValidationResult subResult = subRequirement.validate(schemas);
        if (subResult.isValid()) {
          numberOfValid = numberOfValid + 1;
          System.out.println("valid: " + subRequirement.description);
        }
      }

      if (numberOfValid != 1) {
        result.setValid(false);
        result.setReason(String.format("Only one required: %s", oneOf));
      }
    }

    return result;
  }

  @Setter
  @Getter
  @ToString
  public static class ValidationResult {

    private boolean valid = true;
    private String reason = "";
  }
}