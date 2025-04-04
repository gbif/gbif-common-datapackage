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

import org.gbif.utils.file.ClosableReportingIterator;
import org.gbif.utils.file.csv.CSVReader;
import org.gbif.utils.file.csv.CSVReaderFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.Setter;

/**
 * A delimited text file based source such as CSV or tab files.
 */
public class TextFileSource extends SourceBase implements FileSource {

  private static final Logger LOG = LoggerFactory.getLogger(TextFileSource.class);
  private static final String SUFFIX = ".txt";

  @Getter
  @Setter
  private String fieldsTerminatedBy = "\t";
  @Getter
  @Setter
  private String fieldsEnclosedBy;
  private int ignoreHeaderLines = 0;
  private File file;
  @Setter
  private long fileSize;
  @Setter
  private int rows;
  protected Date lastModified;

  public Character getFieldQuoteChar() {
    if (fieldsEnclosedBy == null || fieldsEnclosedBy.isEmpty()) {
      return null;
    }
    return fieldsEnclosedBy.charAt(0);
  }

  public String getFieldsEnclosedByEscaped() {
    return escape(fieldsEnclosedBy);
  }

  public String getFieldsTerminatedByEscaped() {
    return escape(fieldsTerminatedBy);
  }

  @Override
  public File getFile() {
    return file;
  }

  @Override
  public long getFileSize() {
    return fileSize;
  }

  @Override
  public int getIgnoreHeaderLines() {
    return ignoreHeaderLines;
  }

  @Override
  public Date getLastModified() {
    return lastModified;
  }

  private CSVReader getReader() throws IOException {
    return CSVReaderFactory.build(file, encoding, fieldsTerminatedBy, getFieldQuoteChar(), ignoreHeaderLines);
  }

  @Override
  public int getRows() {
    return rows;
  }

  @Override
  public ClosableReportingIterator<String[]> rowIterator() {
    try {
      return getReader();
    } catch (IOException e) {
      LOG.warn("Exception caught", e);
    }
    return null;
  }

  @Override
  public List<String> columns() {
    try {
      CSVReader reader = getReader();
      if (ignoreHeaderLines > 0) {
        List<String> columns = Arrays.asList(reader.header);
        reader.close();
        return columns;
      } else {
        List<String> columns = new ArrayList<>();
        // careful - the reader.header can be null. In this case set number of columns to 0
        int numColumns = (reader.header == null) ? 0 : reader.header.length;
        for (int x = 1; x <= numColumns; x++) {
          columns.add("Column #" + x);
        }
        reader.close();
        return columns;
      }
    } catch (IOException e) {
      LOG.warn("Cant read source " + getName(), e);
    }

    return new ArrayList<>();
  }

  public void setFieldsEnclosedByEscaped(String fieldsEnclosedBy) {
    this.fieldsEnclosedBy = unescape(fieldsEnclosedBy);
  }

  public void setFieldsTerminatedByEscaped(String fieldsTerminatedBy) {
    this.fieldsTerminatedBy = unescape(fieldsTerminatedBy);
  }

  @Override
  public void setFile(File file) {
    this.file = file;
  }

  public void setIgnoreHeaderLines(Integer ignoreHeaderLines) {
    this.ignoreHeaderLines = ignoreHeaderLines == null ? 0 : ignoreHeaderLines;
  }

  @Override
  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  @Override
  public String getPreferredFileSuffix() {
    return SUFFIX;
  }

  @Override
  public Set<Integer> analyze() throws IOException {
    setFileSize(getFile().length());

    CSVReader reader = getReader();
    while (reader.hasNext()) {
      reader.next();
    }
    setColumns(reader.header == null ? 0 : reader.header.length);
    setRows(reader.getReadRows());
    setReadable(true);
    Set<Integer> emptyLines = reader.getEmptyLines();
    reader.close();
    return emptyLines;
  }

  @Override
  public SourceType getSourceType() {
    return SourceType.TEXT_FILE;
  }
}
