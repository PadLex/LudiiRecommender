package approaches.tree;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import game.Game;
import compiler.Compiler;
import grammar.Grammar;
import parser.Expander;
import parser.Parser;
import main.FileHandling;
import main.StringRoutines;
import main.grammar.Baptist;
import main.grammar.Description;
import main.grammar.Report;
import main.grammar.ebnf.EBNF;
import main.grammar.ebnf.EBNFClause;
import main.grammar.ebnf.EBNFClauseArg;
import main.grammar.ebnf.EBNFRule;
import main.options.UserSelections;
import other.context.Context;
import other.move.Move;
import other.trial.Trial;

//-----------------------------------------------------------------------------

/**
 * Random ludeme generator.
 * 
 * @author cambolbro
 */
public class CompleteGrammarGenerator
{
	/** 
	 * First item in each entry is the relevant ludeme name, the second item
	 * is the list of strings that can be used in it wherever a <string> 
	 * parameter is expected. Repeat strings that you want to occur more often.  
	 */
	private final static String[][][] stringPool =
		{
			// Player 
			{
				{ // Ludemes: 
					"player",  
				},
				{ // Strings:
					"A", "A", "A", "A", "B", "B", "B", "B", "C", "C", "D",
				}
			},
			
			// Piece 
			{
				{ // Ludemes: 
					"piece", "hop", "slide", "fromTo", "place", "leap", "step", 
					"shoot", "promotion", "count",  
				},
				{ // Strings: 
					"Disc", "Disc", "Disc", "Disc", "Disc0", "Disc0", "Disc1", "Disc1",  
					"Disc2", "Disc2", "Disc3", "Disc3", "Disc4", "Disc4", "Disc5", "Disc6", 
					"DiscA", "DiscB", "DiscA1", "DiscB1", "DiscA2", "DiscB2",   
					"Pawn", "Pawn", "Pawn0", "Pawn1", "Pawn2", "Pawn3", "Pawn4", 
					"King", "King0", "King1", "King2", "King3",
				}
			},
			
			// Region 
			{
				{ // Ludemes: 
					"regions", "region", "sites",   
				},
				{ // Strings: 
					"Region", "Region0", "Region1", "Region2", "Region3", "Region4",    
				}
			},
			
			// Track 
			{
				{ // Ludemes:
					"track",    
				},
				{ // Strings:
					"Track", "Track0", "Track1", "Track2", "Track3", "Track4",    
				}
			},
			
			// Vote 
			{
				{ // Ludemes:
					"vote",    
				},
				{ // Strings:
					"Yes", "No", "Maybe",     
				}
			},
			
			// Propose 
			{
				{ // Ludemes:
					"propose",    
				},
				{ // Strings:
					"Win", "Draw", "Loss", "Tie", "Pass",     
				}
			},
			
			// Hints 
			{
				{ // Ludemes:
					"hints",    
				},
				{ // Strings:
					"Hints", "Hints0", "Hints1", "Hints2", "Hints3",      
				}
			},
		};
	
	
	private HashSet<EBNFRule> allowedRules = new HashSet<>();

	
	/** Maximum search depth. */
	private final static int MAX_DEPTH = 100;
	
	/** Point at which to stop random playouts. */
	private final static int MAX_MOVES = 2000;
	
	private final DecisionMaker decisionMaker;
	
	//-------------------------------------------------------------------------

	
	public CompleteGrammarGenerator(DecisionMaker decisionMaker) {
		this.decisionMaker = decisionMaker;
	}
	
	/**
	 * @param ruleName Rule name.
	 * @param seed     RNG seed.
	 * @return Ludemeplex description generated by completing this rule.
	 */
	public String generate(final String ruleName, final long seed)
	{
		
		final List<EBNFRule> rules = findRules(ruleName);
		
		if (rules.isEmpty())
		{
			System.out.println("** Rule " + ruleName + " could not be found.");
			return null;
		}
		
//		System.out.println("Rules:");
//		for (final EBNFRule rule : rules)
//			System.out.println("- " + rule);
		
		String ludeme = complete(decisionMaker.chooseRule(rules), 0);
			
		// Format nicely
		final Report report = new Report();
		final Description description = new Description(ludeme);
		
		Expander.expand(description, new UserSelections(new ArrayList<String>()), report, false);
		
		if (report.isError())
			return ludeme;
		
		return description.expanded();
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Returns list of rules that match a given name.
	 */
	static List<EBNFRule> findRules(final String ruleName)
	{
		final List<EBNFRule> list = new ArrayList<EBNFRule>();
		
		final EBNF ebnf = Grammar.grammar().ebnf();
		
		String str = StringRoutines.toDromedaryCase(ruleName);
		
		// Try exact match
		EBNFRule rule = ebnf.rules().get(str);
		if (rule != null)
		{
			list.add(rule);
			return list;
		}
		
		// Try match with rule delimiters '<...>'
		rule = ebnf.rules().get("<" + str + ">");
		if (rule != null)
		{
			list.add(rule);
			return list;
		}
		
		// Find any match
		if (str.charAt(0) == '<')
			str = str.substring(1);
		if (str.charAt(str.length() - 1) == '>')
			str = str.substring(0, str.length() - 1);
		
		for (final EBNFRule ruleN : ebnf.rules().values())
		{
			String strN = ruleN.lhs();
			if (strN.charAt(0) == '<')
				strN = strN.substring(1);
			if (strN.charAt(strN.length() - 1) == '>')
				strN = strN.substring(0, strN.length() - 1);
			
			while (strN.contains("."))
			{
				final int c = strN.indexOf(".");
				strN = strN.substring(c + 1);
			}
			
			if (strN.equalsIgnoreCase(str))
				list.add(ruleN);
		}
		
		return list;
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Complete ludemeplex string from a given rule.
	 */
	private String complete(final EBNFRule rule, final int depth)
	{


				
		if (depth > MAX_DEPTH)
		{
			System.out.println("** Maximum depth " + MAX_DEPTH + " exceeded in complete() A.");
			return "";
		}

		// Check for quick exit to limit recursion depth
		if (rule.lhs().equals("<int>"))
		{
			if (decisionMaker.chooseExitWithPrimitive())
			{
				// Stop recursion and return terminal version
				return Integer.toString(decisionMaker.choosePrimitiveInteger(-10, 1000));
			}
		}
		else if (rule.lhs().equals("<boolean>"))
		{
			if (decisionMaker.chooseExitWithPrimitive())
			{
				// Stop recursion and return terminal version
				return Boolean.toString(decisionMaker.choosePrimitiveBoolean());
			}
		}
		else if (rule.lhs().equals("<float>"))
		{
			if (decisionMaker.chooseExitWithPrimitive())
			{
				// Stop recursion and return terminal version
				return Float.toString(decisionMaker.choosePrimitiveFloat(-10, 1000));
			}
		}
		else if (rule.lhs().equals("<dim>"))
		{
			// **
			// ** Note: Limit dim recursion or (board ...) ludemes are never completed!
			// **
			if (decisionMaker.chooseExitWithPrimitive())
			{
				// Stop recursion and return terminal version
				return Integer.toString(decisionMaker.choosePrimitiveInteger(0, 20));
			}
		}

		if (rule.rhs() == null || rule.rhs().isEmpty())
		{
			System.out.println("** Rule has no clauses: " + rule);
			return "";
		}

		final EBNFClause clause = decisionMaker.chooseClause(rule.rhs());
		
//		System.out.println("clause: " + clause + " " + (clause.isTerminal() ? "T" : "") +  (clause.isRule() ? "R" : "") +  (clause.isConstructor() ? "C" : ""));
		
		if (clause.isTerminal())
		{
			// Return complete clause immediately
			return clause.toString();
		}
		
		if (clause.isRule())
		{				
			final List<EBNFRule> clauseRule = findRules(clause.token());
			if (clauseRule.isEmpty())
			{
				System.out.println("** Clause has no rule match: " + clause.token());
				return "?";
			}
			else if (clauseRule.size() > 1)
			{
				System.out.println("** Clause has more than one rule match: " + clause.token());
				return "?";
			}
			
			if (depth + 1 >= MAX_DEPTH)
			{
				//System.out.println("** Maximum depth " + MAX_DEPTH + " reached in complete() B.");
				return "";
			}
				
			return complete(decisionMaker.chooseRule(clauseRule), depth + 1);
		}
		
		// Clause must be a constructor at this point
		
		return handleConstructor(clause, depth);
	}
	
	//-------------------------------------------------------------------------

	String handleConstructor(final EBNFClause clause, final int depth)
	{
		//System.out.println(depth);
		
		if (depth > MAX_DEPTH)
		{
			System.out.println("** Maximum depth " + MAX_DEPTH + " exceeded in handleConstructor(). A");
			return "";
		}

		String str = "(" + clause.token();
		
		// TODO figure out what this means
		// Determine 'or' groups
		final int MAX_OR_GROUPS = 10;
		final BitSet[] orGroups = new BitSet[MAX_OR_GROUPS];
		for (int n = 0; n < MAX_OR_GROUPS; n++)
			orGroups[n] = new BitSet();
		
		for (int a = 0; a < clause.args().size(); a++)
		{
			final EBNFClauseArg arg = clause.args().get(a);
			orGroups[arg.orGroup()].set(a);
		}
		
		for (int n = 1; n < MAX_OR_GROUPS; n++)
			while (orGroups[n].cardinality() > 1)
				orGroups[n].set(decisionMaker.chooseOrGroup(clause.args().size()), false);

		// Determine which args to use
		final BitSet use = new BitSet();
		for (int a = 0; a < clause.args().size(); a++)
			for (int n = 0; n < MAX_OR_GROUPS; n++)
				if (orGroups[n].get(a))
					use.set(a);
		
		// Turn off some optional arguments (the deeper, the more likely)
		for (int a = 0; a < clause.args().size(); a++)
			if (clause.args().get(a).isOptional() && decisionMaker.chooseDropArgument())
				use.set(a, false);
		
		for (int a = 0; a < clause.args().size(); a++)
		{
			final EBNFClauseArg arg = clause.args().get(a);
	
			if (!use.get(a))
				continue;  // skip this arg			
			
			if (arg.parameterName() == null)
				str += " ";
			else
				str += " " + arg.parameterName() + ":";
			
			if (arg.nesting() == 0)
			{
				// Not an array
				final String argStr = handleArg(arg, depth);
				if (argStr == "")
					return "";  // maximum depth reached
				
				str += argStr;
			}
			else
			{
				// Handle array (might have nested sub-arrays)				
				str += "{";
				final int numItems = decisionMaker.chooseItemCount();
				for (int i = 0; i < numItems; i++)
				{
					if (arg.nesting() == 1)
					{
						// Single array
						final String argStr = handleArg(arg, depth);
						if (argStr == "")
							return "";  // maximum depth reached
						
						str += " " + argStr;
					}
					else if (arg.nesting() == 2)
					{
						// Nested sub-arrays
						str += " {";
						final int numSubItems = decisionMaker.chooseItemCount();	
						for (int j = 0; j < numSubItems; j++)
						{
							final String argStr = handleArg(arg, depth);
							if (argStr == "")
								return "";  // maximum depth reached
							
							str += " " + argStr;
						}
						str += " }";
					}
					else
					{
						//System.out.println("** Generator.handleConstructor(): Three dimensional arrays not support yet.");
					}
				}
				str += " }";
			}
		}		
		str += ")";
		return str;
	}
	
	String handleArg(final EBNFClauseArg arg, final int depth)
	{			
		if (depth > MAX_DEPTH)
		{
			System.out.println("** Maximum depth " + MAX_DEPTH + " exceeded in handleArg() A.");
			return "";
		}

		if (EBNF.isTerminal(arg.token()))
		{
			// Simply instantiate here rather than recursing, to catch 'string'
			// (also catches 'int', 'boolean' and 'float).
			return arg.token();  //toString();
		}
		
		final List<EBNFRule> argRule = findRules(arg.token());
		if (argRule.isEmpty())
		{
			System.out.println("** Clause arg has no rule match: " + arg.token());
			return "?";
		}
		else if (argRule.size() > 1)
		{
			System.out.println("** Clause arg has more than one rule match: " + arg.token());
			return "?";
		}
		
		if (depth + 1 >= 1000)
		{
			System.out.println("** Safe generation depth " + depth + " exceeded in handleArg() B.");
			return "";
		}
				
		return complete(argRule.get(0), depth + 1);
	}

	//-------------------------------------------------------------------------


		
	static String instantiateStrings(final String input, final Random rng)
	{
		String str = input.trim();
		
		// Instantiate 'string' placeholders
		int c = 0;
		while (true)
		{
			// Find next occurrence of 'string'
			c = str.indexOf("string", c + 1);
			if (c < 0)
				break;
			
			final char chPrev = str.charAt(c - 1);
			final char chNext = str.charAt(c + 6);
			
			if 
			(
				chPrev != ' ' && chPrev != ':' && chPrev != '{'
				||
				chNext != ' ' && chNext != ')' && chNext != '}'
			)
			{
				continue;  // is not an actual string placeholder
			}
			
			final String owner = enclosingLudemeName(str, c);
			//System.out.println("owner='" + owner + "'");
			
			String replacement = null;
			if 
			(
				owner.equalsIgnoreCase("game")
				||
				owner.equalsIgnoreCase("match")
				||
				owner.equalsIgnoreCase("subgame")
			)
			{
				// Create a name for this game
				replacement = Baptist.baptist().name(str.hashCode(), 4);
			}
			
			if (replacement == null)
			{
				// Look for a ludeme from the stringPool
				for (int group = 0; group < stringPool.length && replacement == null; group++)
					for (int n = 0; n < stringPool[group][0].length && replacement == null; n++)
						if (owner.equalsIgnoreCase(stringPool[group][0][n]))
							replacement = stringPool[group][1][rng.nextInt(stringPool[group][1].length)];
			}
			
			if (replacement == null)
			{
				// Create random coordinate
				replacement = (char)('A' + rng.nextInt(26)) + ("" + rng.nextInt(26));
			}
			
			str = str.substring(0, c) + "\"" + replacement + "\"" + str.substring(c + 6);
		}
						
		return str;
	}
	
		
	//-------------------------------------------------------------------------

	/**
	 * @return Name of enclosing ludeme (if any).
	 */
	static String enclosingLudemeName(final String str, final int fromIndex)
	{
		int c = fromIndex;
		while (c >= 0 && str.charAt(c) != '(')
			c--;
		
		if (c < 0)
			return str.split(" ")[0];
		
		int cc = c + 1;
		while (cc < str.length() && str.charAt(cc) != ' ')
			cc++;
		
		return str.substring(c + 1, cc);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * @param numGames 				The number of games to generate.
	 * @param randomSeed			If the seed should be randomised.
	 * @param isValid				Only the games with all the requirement and not satisfying the WillCrash test.
	 * @param boardlessIncluded	    If we try to generate some boardless games.
	 * @param doSave				Whether to save generated games. 
	 * @return						The last valid game description. 
	 */
	public String testGames
	(
		final int numGames, final boolean randomSeed,
		final boolean isValid, final boolean boardlessIncluded, final boolean doSave
	)
	{
		Grammar.grammar().ebnf();  // trigger grammar and EBNF structure to be created

		
		final long startAt = System.currentTimeMillis();
				
		int numValid = 0;
		int numParse = 0;
		int numCompile = 0;
		int numFunctional = 0;
		int numPlayable = 0;
		
		final Report report = new Report();
		
		// Keep a record of the last valid, playable, generated game, which will be returned at the end.
		String lastGeneratedGame = null;
		
		final Random rng = new Random();
		
		// Generate games
		for (int n = 0; n < numGames; n++)
		{
			System.out.println("\n---------------------------------\nGame " + n + ":");
			
			final String str = generate("game", randomSeed ? rng.nextLong() : n);
			System.out.println(str);
			
			if (str == "")
				continue;  // generation failed
			
			numValid++;
			
			String gameName = StringRoutines.gameName(str);
			if (gameName == null)
				gameName = "Anon";
			final String fileName = gameName + ".lud";  // just in case...
			
			// Check whether game parses
			final Description description = new Description(str);	
			final UserSelections userSelections = new UserSelections(new ArrayList<String>());
			
			Parser.expandAndParse(description, userSelections, report, false);	
			if (report.isError())
			{
				// Game does not parse
				if (doSave)
					FileHandling.saveStringToFile(str, "../Common/res/lud/test/buggy/unparsable/", fileName);
				continue;
			}			
			//System.out.println("Parses...");
			numParse++;
			
			if (!boardlessIncluded)
			{
				//if (game.isBoardless())
				if (description.expanded().contains("boardless"))
					continue;
			}

			for (final String warning : report.warnings())
				if (!warning.contains("No version info."))
					System.out.println("- Warning: " + warning);
			
			// Check whether game compiles
			Game game = null;
			try
			{
				game = (Game)Compiler.compileTest(new Description(str), false);
			}
			catch (final Exception e)
			{
				for (final String error : report.errors())
					System.out.println("- Error: " + error);

				for (final String warning : report.warnings())
					if (!warning.contains("No version info."))
						System.out.println("- Warning: " + warning);

				e.printStackTrace();
			}

			if (game == null)
			{
				// Game does not compile
				if (doSave)
					FileHandling.saveStringToFile(str, "../Common/res/lud/test/buggy/uncompilable/", fileName);
				continue;
			}
			//System.out.println("Compiles...");			
			numCompile++;

			if (isValid)
			{
				if (game.hasMissingRequirement() || game.willCrash())
					continue;
				System.out.println("Not known to crash...");
			}
			
//			if (!boardlessIncluded)
//			{
//				//if (game.isBoardless())
//				if (description.expanded().contains("boardless"))
//					continue;
//				System.out.println("Parses...");
//			}

			// Check whether game is functional
			try
			{
				if (!isFunctional(game))
				{
					// Game is not functional
					if (doSave)
						FileHandling.saveStringToFile(str, "../Common/res/lud/test/buggy/nonfunctional/", fileName);
					continue;
				}
			}
			catch (final Exception e)
			{
				System.out.println("Handling exception during playability test.");
				e.printStackTrace();
			}
			
			//System.out.println("Is functional...");
			numFunctional++;
			
			// Check whether game is playable
			if (!isPlayable(game))
			{
				// Game is not playable
				if (doSave)
					FileHandling.saveStringToFile(str, "../Common/res/lud/test/buggy/unplayable/", fileName);
				continue;
			}
			System.out.println("Is playable.");
			numPlayable++;

			//if (doSave)
				FileHandling.saveStringToFile(str, "../Common/res/lud/test/playable/", fileName);
			
			lastGeneratedGame = str;
		}	
		
		final double secs = (System.currentTimeMillis() - startAt) / 1000.0;
		
		System.out.println
		(
			"\n===========================================\n"
			+
			numGames + " random games generated in " + secs + "s:\n"
			+
			numValid + " valid (" + (numValid * 100.0 / numGames) + "%).\n"
			+
			numParse + " parse (" + (numParse * 100.0 / numGames) + "%).\n"
			+
			numCompile + " compile (" + (numCompile * 100.0 / numGames) + "%).\n"
			+
			numFunctional + " functional (" + (numFunctional * 100.0 / numGames) + "%).\n"
			+
			numPlayable + " playable (" + (numPlayable * 100.0 / numGames) + "%).\n"
		);
		
		return lastGeneratedGame;
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * Method from Eric to improve some generations did with Ludii.
	 * 
	 * @param numGames             The number of games to generate.
	 * @param dlpRestriction       If we apply some DLP restrictions (like no
	 *                             boardless, cards etc...)
	 * @param withDecision If we keep the games with only decision moves.
	 * @return The last valid game description.
	 */
	public static String testGamesEric
	(
			final CompleteGrammarGenerator generator,
		final int numGames, final boolean dlpRestriction,
		final boolean withDecision
	)
	{
		Grammar.grammar().ebnf();  // trigger grammar and EBNF structure to be created
	
		
		final long startAt = System.currentTimeMillis();
				
		final Report report = new Report();
		
		// Keep a record of the last valid, playable, generated game, which will be returned at the end.
		String lastGeneratedGame = null;
		
		final Random rng = new Random();
		
		// Generate games
		int n = 0;
		int numTry = 0;
		while(n < numGames)
		{
			final String str = generator.generate("game", rng.nextLong());
			
			if (str == "")
			{
				numTry++;
				// System.out.println("Generation failed for try " + numTry);
				continue;  // generation failed
			}
			
			final boolean containsPlayRules = str.contains("(play");
			final boolean containsEndRules = str.contains("(end");
			final boolean containsMatch = str.contains("(match");

			if (!containsPlayRules || !containsEndRules || containsMatch)
			{
				numTry++;
				// System.out.println("Game is a match or does not generate a play and end rule
				// " + numTry);
				continue;
			}

			// Check whether game parses
			final Description description = new Description(str);	
			final UserSelections userSelections = new UserSelections(new ArrayList<String>());
			
			Parser.expandAndParse(description, userSelections, report, false);	
			if (report.isError())
			{
				// Game does not parse
				// FileHandling.saveStringToFile(str,
				// "../Common/res/lud/test/buggy/unparsable/", fileName);

				numTry++;
				// System.out.println("Game unparsable for try " + numTry);
				continue;
			}
			
//			for (final String warning : report.warnings())
//				if (!warning.contains("No version info."))
//					System.out.println("- Warning: " + warning);
			
			// Check whether game compiles
			Game game = null;
			try
			{
				game = (Game)Compiler.compileTest(new Description(str), false);
			}
			catch (final Exception e)
			{
				// Nothing to do.
			}

			if (game == null)
			{
				// Game does not compile
				numTry++;
				// System.out.println("Game uncompilable for try " + numTry);
				continue;
			}
			
			if (game.hasMissingRequirement() || game.willCrash())
			{
				numTry++;
				// System.out.println("Game with warning and possible crash for try " + numTry);
				continue;
			}
			
			// No boardless, No match, no deduc Puzzle, cards, dominoes or large pieces, no
			// hidden info.
			// Only Alternating game.
			if (dlpRestriction)
				if (game.hasSubgames() || game.isBoardless() || game.isDeductionPuzzle() || game.hasCard()
						|| game.hasDominoes() || game.hasLargePiece() || !game.isAlternatingMoveGame()
						|| game.hiddenInformation()
				)
				{
					numTry++;
					// System.out.println("Game not satisfying for try " + numTry);
					continue;
				}

			final String fileName = game.name() + ".lud";
			
			// Check whether game is functional
			if (withDecision)
			{
				if (!isFunctionalAndWithOnlyDecision(game))
				{
					// Game is not functional
					numTry++;
					// System.out.println("Game non functional or has a move with no decision for
					// try " + numTry);
					continue;
				}
			}
			else if (!isFunctional(game))
			{
				// Game is not functional
				numTry++;
				FileHandling.saveStringToFile(str, "../Common/res/lud/test/buggy/nonfunctional/", fileName);
				// System.out.println("Game non functional for
				// try " + numTry);
				continue;
			}
			
			// Check whether game is playable
			if (!isPlayable(game))
			{
				// Game is not playable
				numTry++;
				FileHandling.saveStringToFile(str, "../Common/res/lud/test/buggy/unplayable/", fileName);
				// System.out.println("Game unplayable for try " + numTry);
				continue;
			}
			else
			{
				FileHandling.saveStringToFile(str, "../Common/res/lud/test/buggy/toTest/", fileName);
				System.out.println("GAME " + n + " GENERATED");
				numTry++;
				n++;
			}
			
			lastGeneratedGame = str;
		}	
		
		final double secs = (System.currentTimeMillis() - startAt) / 1000.0;
		
		System.out.println("Generation done in " + secs + " seconds");
		System.out.println(numTry + " tries were necessary.");
		
		return lastGeneratedGame;
	}

	/**
	 * @return Whether a trial of the game can be played without crashing.
	 */
	public static boolean isFunctionalAndWithOnlyDecision(final Game game)
	{
		final Context context = new Context(game, new Trial(game));
		game.start(context);
		Trial trial = null;
		try
		{
			trial = game.playout
					(
						context, null, 1.0, null, 0, MAX_MOVES, 
						ThreadLocalRandom.current()
					);
		}
		catch(final Exception e)
		{
			e.printStackTrace();
		}
		
		if(trial == null)
			return false;
		
		for(final Move m : trial.generateCompleteMovesList())
			if(!m.isDecision())
				return false;
		
		return true;	
	}
	
	//-------------------------------------------------------------------------

	/**
	 * @return Whether a trial of the game can be played without crashing.
	 */
	public static boolean isFunctional(final Game game)
	{
		final Context context = new Context(game, new Trial(game));
		game.start(context);
		Trial trial = null;
		try
		{
			trial = game.playout
					(
						context, null, 1.0, null, 0, MAX_MOVES, 
						ThreadLocalRandom.current()
					);
		}
		catch(final Exception e)
		{
			e.printStackTrace();
		}
		return trial != null; 		
	}

	//-------------------------------------------------------------------------

	/**
	 * @return Whether the game is basically playable, i.e. more trials are of
	 *         reasonable length than not. A trial is of reasonable length if it
	 *         lasts at least 2 * num_players moves and ends before 90% of MAX_MOVES are reached.
	 */
	public static boolean isPlayable(final Game game)
	{
		final Context context = new Context(game, new Trial(game));
		
		final int NUM_TRIALS = 10;
		
		int numResults = 0;
		
		for (int t = 0; t < NUM_TRIALS; t++)
		{		
			game.start(context);
			Trial trial = null;
			try
			{
				trial = game.playout
						(
							context, null, 1.0, null, 0, MAX_MOVES, 
							ThreadLocalRandom.current()
						);
			}
			catch(final Exception e)
			{
				e.printStackTrace();
			}
			
			//System.out.println(trial.numMoves() + " moves in " + trial.numberOfTurns() + " turns.");
			
			if (trial == null)
				return false;

			final int minMoves = 2 * game.players().count();
			final int maxMoves = MAX_MOVES * 9 / 10;
			
			if (trial.numMoves() >= minMoves && trial.numMoves() <= maxMoves)
				numResults++;
		}
		
		return numResults >= NUM_TRIALS / 2; 		
	}
	
	//-------------------------------------------------------------------------

	void test()
	{
		Grammar.grammar().ebnf();  // trigger grammar and EBNF structure to be created
			
		// Check rules
		int numClauses = 0;
		for (final EBNFRule rule : Grammar.grammar().ebnf().rules().values())
		{	
			numClauses += rule.rhs().size();
			for (final EBNFClause clause : rule.rhs())		
			{	
				//System.out.println("EBNF clause: " + clause);
				
				if (clause != null && clause.args() != null)
					for (final EBNFClauseArg arg : clause.args())
					{
						final List<EBNFRule> list = findRules(arg.token());
						if 
						(
							list.isEmpty() 
							&& 
							!arg.token().equalsIgnoreCase("string")
							&&
							!Character.isUpperCase(arg.token().charAt(0))  // probably a single enum that has been instantiated
						)
						{
							System.out.println("** No rule for: " + arg);
						}
					}
			}
		}
		System.out.println(Grammar.grammar().ebnf().rules().size() + " EBNF rules with " + numClauses + " clauses generated.");
		
//		final Random rng = new Random();
//		final int bound = 2;
//		for (int seed = 0; seed < 100; seed++)
//		{
//			rng.setSeed(seed);
//			//rng.nextInt();
//			for (int n = 0; n < 32; n++)
//				System.out.print(rng.nextInt(bound) + " ");
//			System.out.println();
//		}
		
		System.out.println("===========================================================\n");
		
		final long startAt = System.currentTimeMillis();
				
		// Generate games
		final int NUM_GAMES = 1000;
		for (int n = 0; n < NUM_GAMES; n++)
		{
			final String str = generate("game", n);
			
			if (n % 100 == 0)
			{
				System.out.println(n + ":");
				System.out.println(str == "" ? "** Generation failed.\n" : str);
			}
		}	
		
		final double secs = (System.currentTimeMillis() - startAt) / 1000.0;
		System.out.println(NUM_GAMES + " games generated in " + secs + "s.");
		
		System.out.println("===========================================================\n");
		
		// Generate sub-ludeme
		String str = generate("or", 0);
		System.out.println(str);

		str = generate("or", 1);
		System.out.println(str);
		
		System.out.println("===========================================================\n");
		
		// Generate sub-ludeme
		str = generate("board", System.currentTimeMillis());
		System.out.println(str);

		System.out.println("===========================================================\n");
		
		// Generate enum
		str = generate("ShapeType", 0);
		System.out.println(str);
		
		str = generate("ShapeType", 1);
		System.out.println(str);
	}
	
	//-------------------------------------------------------------------------
	
	/**
	 * The main method of the generator.
	 * 
	 * @param arg
	 */
	public static void main(final String[] arg)
	{
		final DecisionMaker decisionMaker = new HumanDecisionMaker();
		final CompleteGrammarGenerator generator = new CompleteGrammarGenerator(decisionMaker);
		generator.test();		
	}
	
}
