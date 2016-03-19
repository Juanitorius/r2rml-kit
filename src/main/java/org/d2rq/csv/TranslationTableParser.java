package org.d2rq.csv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.d2rq.D2RQException;
import org.d2rq.lang.TranslationTable.Translation;

import com.hp.hpl.jena.n3.IRIResolver;


/**
 * Parses the contents of a CSV file into a collection of
 * <tt>Translation</tt>s. The CVS file must contain exactly
 * two columns. DB values come from the first, RDF values
 * from the second.
 *
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class TranslationTableParser {
	private Log log = LogFactory.getLog(TranslationTableParser.class);
	private BufferedReader reader;
	private CSV csvLineParser = new CSV();
	private String url;

	public TranslationTableParser(Reader reader) {
		this.reader = new BufferedReader(reader);
	}
	
	public TranslationTableParser(String url) {
		try {
			this.url = new IRIResolver().resolve(url);
			this.reader = new BufferedReader(new InputStreamReader(new URL(this.url).openStream()));
		} catch (FileNotFoundException ex) {
			throw new D2RQException("File not found at URL: " + this.url);
		} catch (MalformedURLException ex) {
			throw new D2RQException("Malformed URI: " + this.url);
		} catch (IOException ex) {
			throw new D2RQException("Error reading from translation table: " + this.url, ex);
		}
	}
	
	public Collection<Translation> parseTranslations() {
		try {
			List<Translation> result = new ArrayList<Translation>();
			while (true) {
				String line = this.reader.readLine();
				if (line == null) {
					break;
				}
				String[] fields = this.csvLineParser.parse(line);
				if (fields.length != 2) {
					this.log.warn("Skipping line with " +
							fields.length + " instead of 2 columns in CSV file " + this.url);
					continue;
				}
				result.add(new Translation(fields[0], fields[1]));
			}
			return result;
		} catch (IOException iex) {
			throw new D2RQException(iex);
		}
	}
}