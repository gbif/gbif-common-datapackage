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

import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data schema definition.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataPackageSchema implements Serializable {

  private static final long serialVersionUID = -3130006092545816514L;

  /**
   * Schema unique identifier
   */
  private String identifier;

  /**
   * Schema full title
   */
  private String title;

  /**
   * Schema shortened title
   */
  private String shortTitle;

  /**
   * Schema name for internal usage
   */
  private String name;

  /**
   * Schema version
   */
  private String version;

  /**
   * Schema URL
   */
  private URL url;

  /**
   * Metadata profile
   */
  private String profile;

  private Set<DataPackageTableSchema> tableSchemas = new LinkedHashSet<>();

  private DataPackageTableSchemaRequirement tableSchemasRequirements = new DataPackageTableSchemaRequirement();

  private String description;

  private boolean isLatest;

  public boolean isUpdatable;

  private Date issued;

  public DataPackageTableSchema tableSchemaByName(String tableSchemaName) {
    return tableSchemas.stream()
        .filter(ds -> ds.getName().equals(tableSchemaName))
        .findFirst().orElse(null);
  }

  public void addTableSchema(DataPackageTableSchema tableSchema) {
    tableSchemas.add(tableSchema);
  }
}
