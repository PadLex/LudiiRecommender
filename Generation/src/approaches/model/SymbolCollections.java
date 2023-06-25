package approaches.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import grammar.Grammar;
import main.grammar.Symbol;

public class SymbolCollections {

	public static final HashSet<String> notSoHiddenSymbols = new HashSet<>(Arrays.asList("End", "Graphics", "Phase"));

	public static final HashSet<String> boardGameSymbolNames = new HashSet<>(Arrays.asList("Visited", "Left", "tiling", "wedge", "Repeat", "moveAgain", "Liberties", "score", "path", "Pass", "firstMoveOnTrack", "centrePoint", "pass", "%", "Hop", "*", "+", "-", "/", "Line", "Related", "size", "EndOfTurn", "<", "!=", "=", "Blocked", ">", "A", "handSite", "B", "C", "Set", "D", "E", "ahead", "F", "G", "H", "hop", "roll", "renumber", "I", "J", "BL", "K", "L", "Hexagonal", "trackSite", "M", "N", "O", "result", "P", "BR", "Q", "R", "S", "dual", "T", "Propose", "U", "V", "W", "X", "Y", "MoveLimit", "Z", "flip", "^", "hand", "Players", "WNW", "Die", "set", "column", "spiral", "Columns", "union", "Bet", "abs", "CW", "flips", "Forward", "Add", "Backwards", "Neutral", "Start", "Around", "sites", "do", "Enemy", "intersection", "Undefined", "count", "ToClear", "avoidStoredState", "Hand", "graph", "piece", "State", "allCombinations", "Bottom", "End", "Corners", "ENE", "FromBottom", "DiceUsed", "FL", "Outer", "FR", "UNE", "remove", "end", "Radiating", "UNW", "Diamond", "counter", "LastTo", "hole", "Triggered", "mover", "WSW", "append", "regionSite", "Remove", "Odd", "Different", "Board", "Hidden", "id", "Even", "if", "Distance", "Sides", "Pieces", "is", "Square", "Sites", "boardless", "surakartaBoard", "Top", "Alternating", "rotations", "skew", "Vertex", "DiceEqual", "Draw", "In", "Right", "Direction", "mapEntry", "Off", "Side", "SameLayer", "Slide", "ESE", "directional", "MovesThisTurn", "Threatened", "T33434", "surround", "map", "Orthogonal", "max", "USE", "trigger", "Leftward", "Playable", "USW", "All", "pathExtent", "rotate", "pips", "while", "Concentric", "Vertices", "Counter", "Random", "Swap", "claim", "sow", "Team2", "Team1", "Cell", "all", "Player", "Occupied", "level", "swap", "Between", "equipment", "tri", "arrayValue", "expand", "forget", "directions", "NE", "Value", "Row", "TeamMover", "no", "Phase", "NW", "EndSite", "and", "repeat", "row", "Rows", "Adjacent", "P1", "P2", "toInt", "Hexagon", "or", "P3", "P4", "SSE", "P5", "forEach", "custodial", "P6", "P7", "P8", "Centre", "Array", "Diagonal", "SSW", "Pips", "Incident", "nextPhase", "Edge", "Track", "Each", "Connected", "shift", "prev", "Inner", "Farthest", "sizes", "merge", "state", "SidesMatch", "AnyDie", "mancalaBoard", "then", "Rotational", "Pot", "meta", "NextPlayer", "difference", "NotEmpty", "Group", "PositionalInTurn", "Solid", "remember", "promote", "SE", "min", "pot", "Star", "Piece", "Backward", "Groups", "Limping", "where", "Team", "SW", "face", "LineOfSight", "Prev", "topLevel", "Select", "makeFaces", "to", "intervene", "Passed", "NonMover", "NNE", "rules", "Full", "NNW", "tile", "To", "results", "Territory", "last", "FirstSite", "Stack", "SameDirection", "Score", "next", "Forwards", "attract", "Step", "What", "Mover", "Empty", "not", "Moves", "concentric", "vote", "enclose", "FromTop", "Loop", "Perimeter", "apply", "start", "was", "Rotation", "T3636", "pair", "Captures", "what", "OppositeDirection", "Within", "Decided", "step", "play", "Var", "LargePiece", "Rectangle", "Triangle", "Count", "Shared", "hex", "between", "phase", "Vote", "players", "byScore", "var", "Proposed", "priority", "Cycle", "push", "Out", "slide", "Next", "exact", "Level", "Shoot", "Site", "regions", "NoEnd", "range", "fromTo", "layer", "coord", "Friend", "leap", "place", "regular", "Pattern", "Loss", "Own", "Promote", "site", "None", "board", "<=", "Rightward", "T3464", "dice", "from", "LevelTo", "Who", "Turns", "SidesNoCorners", "Leap", "square", "Active", "keep", "Flat", "rectangle", "Win", "Prism", "who", ">=", "Steps", "LastSite", "RememberValue", "note", "game", "passEnd", "values", "scale", "automove", "can", "Move", "Column", "array", "addScore", "splitCrossings", "Remembered", "track", "value", "player", "move", "amount", "TurnLimit", "From", "CCW", "poly", "Pending"));
	
	public static final HashSet<String> smallBoardGameSymbolNames = new HashSet<>(Arrays.asList("Each", "Left", "Connected", "shift", "tiling", "wedge", "Repeat", "moveAgain", "Liberties", "score", "Farthest", "path", "Pass", "sizes", "merge", "state", "SidesMatch", "centrePoint", "%", "pass", "mancalaBoard", "Hop", "*", "then", "+", "Rotational", "-", "/", "Line", "Related", "size", "meta", "NextPlayer", "difference", "NotEmpty", "<", "!=", "=", "Blocked", ">", "A", "Group", "handSite", "B", "C", "Set", "D", "E", "ahead", "F", "G", "H", "hop", "roll", "renumber", "I", "J", "K", "L", "Hexagonal", "trackSite", "M", "N", "Solid", "O", "result", "remember", "P", "promote", "SE", "R", "S", "dual", "min", "T", "Star", "Propose", "U", "Piece", "Groups", "V", "W", "Limping", "X", "Y", "where", "MoveLimit", "Team", "Z", "flip", "^", "hand", "Players", "WNW", "Die", "set", "SW", "Infinity", "column", "Columns", "union", "LineOfSight", "Prev", "topLevel", "flips", "Select", "makeFaces", "Forward", "to", "intervene", "Passed", "Add", "Backwards", "Neutral", "NonMover", "Around", "rules", "sites", "do", "Full", "Enemy", "intersection", "tile", "To", "results", "Undefined", "Territory", "last", "count", "FirstSite", "ToClear", "Hand", "graph", "Stack", "SameDirection", "Score", "piece", "State", "allCombinations", "Bottom", "End", "Corners", "ENE", "next", "DiceUsed", "FL", "Forwards", "attract", "Step", "Outer", "FR", "UNE", "remove", "Mover", "Empty", "not", "Moves", "concentric", "end", "Radiating", "vote", "UNW", "enclose", "FromTop", "Loop", "Perimeter", "intersect", "apply", "start", "Diamond", "counter", "pair", "Captures", "hole", "Triggered", "what", "mover", "Within", "Decided", "step", "WSW", "append", "play", "Var", "Triangle", "Rectangle", "Remove", "Count", "Shared", "Odd", "Board", "Hidden", "hex", "id", "Even", "if", "between", "Distance", "Sides", "Pieces", "phase", "Vote", "players", "byScore", "var", "is", "Proposed", "priority", "Square", "Cycle", "push", "Sites", "surakartaBoard", "boardless", "Top", "Next", "exact", "Alternating", "Level", "Shoot", "rotations", "skew", "Site", "Vertex", "DiceEqual", "regions", "Draw", "NoEnd", "In", "Right", "range", "Direction", "fromTo", "mapEntry", "Off", "layer", "Side", "coord", "Friend", "SameLayer", "Slide", "ESE", "MovesThisTurn", "place", "Threatened", "T33434", "surround", "map", "regular", "Pattern", "Orthogonal", "max", "USE", "Loss", "Own", "trigger", "site", "Leftward", "Playable", "USW", "None", "board", "All", "pathExtent", "rotate", "<=", "Rightward", "T3464", "dice", "pips", "while", "Counter", "Random", "Swap", "from", "sow", "Team2", "Team1", "Cell", "all", "Turns", "Player", "Occupied", "swap", "level", "SidesNoCorners", "equipment", "tri", "Leap", "square", "expand", "forget", "directions", "keep", "NE", "Value", "Flat", "rectangle", "Row", "Win", "Prism", "who", ">=", "Steps", "LastSite", "no", "note", "game", "Phase", "passEnd", "values", "scale", "NW", "EndSite", "automove", "can", "subdivide", "Move", "Column", "array", "and", "splitCrossings", "addScore", "repeat", "row", "Remembered", "track", "value", "player", "Rows", "Adjacent", "P1", "move", "P2", "Hexagon", "or", "P3", "P4", "SSE", "P5", "forEach", "custodial", "P6", "TurnLimit", "From", "Centre", "Diagonal", "SSW", "poly", "Pips", "Incident", "nextPhase", "Edge", "Pending", "Track"));
	
	public static List<Symbol> completeGrammar() {
		return filterHidden(Grammar.grammar().symbols());
	}
	
	public static List<Symbol> boardGames() {
		return filterByName(Grammar.grammar().symbols(), boardGameSymbolNames);
	}
	
	public static List<Symbol> smallBoardGames() {
		return filterByName(Grammar.grammar().symbols(), smallBoardGameSymbolNames);
	}
	
	public static List<Symbol> filterHidden(List<Symbol> symbols) {
		return symbols.stream().filter(s -> s.usedInGrammar() || notSoHiddenSymbols.contains(s.name().replace("<", "").replace(">", ""))).collect(Collectors.toList());
	}
	
	public static List<Symbol> filterByName(List<Symbol> symbols, HashSet<String> names) {
		return symbols.stream().filter(s -> names.contains(s.name().replace("<", "").replace(">", ""))).collect(Collectors.toList());
	}
}