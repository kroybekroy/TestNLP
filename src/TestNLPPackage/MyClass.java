package TestNLPPackage;

import java.io.*;
import java.nio.charset.*;
import java.sql.*;
import java.util.regex.*;

import opennlp.tools.chunker.*;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.doccat.*;
import opennlp.tools.namefind.*;
import opennlp.tools.parser.*;
import opennlp.tools.postag.*;
import opennlp.tools.sentdetect.*;
import opennlp.tools.tokenize.*;
import opennlp.tools.util.*;

public class MyClass {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String query = "";
		query = "SELECT id, body FROM `sms` WHERE address NOT LIKE '+%'";
		
		String fileName = "";
		fileName = "NLP.txt"; 

		try {
			ResultSet objResultSet = GetData(query);

			/* Unblock this code if you want to write to a text file */
			 try 
			 { 
				 	PrintStream out = new PrintStream(new FileOutputStream(fileName)); 
				 	System.setOut(out); 
			 } 
			 catch (FileNotFoundException e) 
			 { 
				 // TODO Auto-generated catch block 
				 e.printStackTrace();
			 }
			 

			while (objResultSet.next()) {
				String data = objResultSet.getString("body");
			 	//String data = "Thank you for using StanChart Card No XX4275 on 05/04/15 for INR 842.70. To check EMI eligibility on spends above INR5000, log on to m.sc.com/in -T&C Apply.";
			 
				System.out.print(data);

				 int score = ValidateData(data);
				 
				 if (score >= 2) {
					 
					//SentenceDetectorMethod(data);

					String tokens[] = TokenizerMethod(data);

					String tags[] = POSTaggerMethod(tokens);				

					 //NameFinderMethod(tokens);

					 //DocumentCategorizerMethod(data);

					 String chunks[] = ChunkerMethod(tokens, tags);
					 
					//ParserMethod(data);
						
					 //ShowData(" :: CD", fileName);
					 
					for (int i = 0; i < tokens.length; i++) {
						query = "INSERT INTO sms_pos_tag(sms_id, token, tag, chunk) SELECT "
								+ objResultSet.getString("id")
								+ ", '"
								+ tokens[i]
								+ "', '"
								+ tags[i]
								+ "', '"
								+ chunks[i] + "'";

						SetData(query);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String[] SentenceDetectorMethod(String data) {
		InputStream modelIn = null;
		String sentences[] = null;

		try {

			modelIn = new FileInputStream("bin/en-sent.bin");

			SentenceModel model = new SentenceModel(modelIn);

			SentenceDetectorME sentenceDetector = new SentenceDetectorME(model);

			sentences = sentenceDetector.sentDetect(data);

			System.out.print("\n\nSentences\n");

			for (int i = 0; i < sentences.length; i++) {
				System.out.print((i + 1) + ". " + sentences[i] + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}

		return sentences;
	}

	public static String[] TokenizerMethod(String data) {
		InputStream modelIn = null;
		String tokens[] = null;

		try {

			modelIn = new FileInputStream("bin/en-token.bin");

			TokenizerModel model = new TokenizerModel(modelIn);

			TokenizerME tokenizer = new TokenizerME(model);

			tokens = tokenizer.tokenize(data);

			//System.out.print("\n\nTokens\n");

			/*for (int i = 0; i < tokens.length; i++) {
				System.out.print((i + 1) + ". " + tokens[i] + "\n");
			}*/
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}

		return tokens;
	}

	public static String[] POSTaggerMethod(String[] tokens) {
		InputStream modelIn = null;
		String tags[] = null;

		try {

			modelIn = new FileInputStream("bin/en-pos-maxent.bin");
			// modelIn = new FileInputStream("bin/en-pos-perceptron.bin");

			POSModel model = new POSModel(modelIn);

			POSTaggerME tagger = new POSTaggerME(model);

			tags = tagger.tag(tokens);

			//System.out.print("\n\nPOS Tags [Token :: Tag]\n");

			/*for (int i = 0; i < tags.length; i++) {
				System.out.print((i + 1) + ". " + tokens[i] + " :: " + tags[i]
						+ "\n");
			}*/
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}

		return tags;
	}

	public static Span[] NameFinderMethod(String[] sentences) {
		InputStream modelIn = null;
		Span results[] = null;

		try {

			modelIn = new FileInputStream("bin/en-ner-date.bin");

			TokenNameFinderModel model = new TokenNameFinderModel(modelIn);

			NameFinderME nameFinder = new NameFinderME(model);

			results = nameFinder.find(sentences);

			String finds[] = Span.spansToStrings(results, sentences);

			System.out.print("\n\nName Finds\n");

			for (int i = 0; i < finds.length; i++) {
				System.out.print((i + 1) + ". " + finds[i] + "\n");
			}

			nameFinder.clearAdaptiveData();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}

		return results;
	}

	public static void DocumentCategorizerMethod(String sentence) {
		InputStream modelIn = null;

		try {

			modelIn = new ByteArrayInputStream(sentence.getBytes(Charset
					.forName("UTF-8")));

			DoccatModel model = new DoccatModel(modelIn);

			DocumentCategorizerME documentCategorizer = new DocumentCategorizerME(
					model);

			double[] outcomes = documentCategorizer.categorize(sentence);

			String category = documentCategorizer.getBestCategory(outcomes);

			System.out.println(category);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static String[] ChunkerMethod(String[] tokens, String[] tags) {
		InputStream modelIn = null;
		String chunks[] = null;

		try {

			modelIn = new FileInputStream("bin/en-chunker.bin");

			ChunkerModel model = new ChunkerModel(modelIn);

			ChunkerME chunker = new ChunkerME(model);

			chunks = chunker.chunk(tokens, tags);

			double[] probs = chunker.probs();

			//System.out.print("\n\nChunks [Token :: Tag :: Chunk :: Confidence Probability]\n");

			/*for (int i = 0; i < chunks.length; i++) {
				System.out.print((i + 1) + ". " + tokens[i] + " :: " + tags[i]
						+ " :: " + chunks[i] + " :: " + probs[i] + "\n");
			}*/
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}

		return chunks;
	}

	public static Parse[] ParserMethod(String data) {
		InputStream modelIn = null;
		Parse parses[] = null;

		try {

			modelIn = new FileInputStream("bin/en-parser-chunking.bin");

			ParserModel model = new ParserModel(modelIn);

			Parser parser = ParserFactory.create(model);

			parses = ParserTool.parseLine(data, parser, 1);

			System.out.print("\n\nParses\n");

			for (int i = 0; i < parses.length; i++) {
				System.out.print((i + 1) + ". ");
				parses[i].show();
				System.out.print("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}

		return parses;
	}

	public static ResultSet GetData(String query) {
		ResultSet objResultSet = null;

		try {
			// String driver = "org.gjt.mm.mysql.Driver";
			String url = "jdbc:mysql://localhost/smsread";

			// Class.forName(driver);

			Connection connection = DriverManager.getConnection(url, "root",
					"Welcome@123");

			Statement statement = connection.createStatement();

			objResultSet = statement.executeQuery(query);

			/*
			 * } catch (ClassNotFoundException e) { // TODO Auto-generated catch
			 * block e.printStackTrace();
			 */
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return objResultSet;
	}

	public static void SetData(String query) {

		try {
			// String driver = "org.gjt.mm.mysql.Driver";

			String url = "jdbc:mysql://localhost/smsread";

			// Class.forName(driver);

			Connection connection = DriverManager.getConnection(url, "root",
					"Welcome@123");

			PreparedStatement statement = connection.prepareStatement(query);

			statement.executeUpdate();

			connection.close();

			/*
			 * } catch (ClassNotFoundException e) { // TODO Auto-generated catch
			 * block e.printStackTrace();
			 */
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void ShowData(String search, String filename) {
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(filename));

			String line = null;

			while ((line = br.readLine()) != null) {
				if (line.contains(search)) {
					System.out.println(line);
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static int ValidateData(String data)
	{		
		int score = 0;
		
		Pattern pCurrency = Pattern.compile("\\d{1,3}(,\\d{2,3})*\\.\\d{1,2}");
		Pattern pNumber = Pattern.compile("[0-9]{4,}");
		
		Matcher mCurrency = pCurrency.matcher(data);
		
		if(mCurrency.find())
			score += 1;
		
		Matcher mNumber = pNumber.matcher(data);
		
		if(mNumber.find())
			score += 1;
		
		return score;
	}
	
}