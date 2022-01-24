# wordle-tinkering
A scratchpad for using Wordle as a learning tool for coding. The intention for this is to 
work up in complexity from a very basic solver through to looking at others' masked results to
see if we can beat the statistical _state-of-the-art_.

## Phases _(hopefully)_

### 1. Basic Game
Provide a simple implementation of the game that separates both the ``Player`` and 
the ``Scorer`` and the main ``Wordle`` game loop such that a Player is prompted to ``guess``
and the scorer ``scores`` the guess. The implementation intends to provide both human
and machine implementations for both API's such that we can:
1. **Get help playing a web game of world** - Manual Player entry and Manual Scorer used for entering grading from a web based game
2. **Allow the computer to play a web game of wordle** - Manual Scorer used for the user to enter grading from a web based game of Wordle, but 
3. **Play local games of wordle** - Known Word Automated Scorer running with a randomly chosen word each round
4. **Machine playing itself** - Known Word Automated Scorer being used to score a Machine guessing agent

**Status: Done** - _but somewhat poorly_

### 2. Candidate Tracker / Constraint solver
What is (I think) a complete working solution of the constraint solver is in ``CandidateTracker``. A few notes:
* **behaviors** - constraint solver does exact characters first, then inexact and no match together with consideration for exact occurrence of repeated letters
* **premature optimization** - I'm encoding the carry-over invalid bitmap as an ``Array<Int>``, this is jumping the gun on efficient cloning of state for later state space encoding
* **legibility** - the code isn't as easy to read as I'd like *(building this with my 6-year-old, so definitely need to do better here)*.

**Status: Done** - I _think_ - it's rather light on testing though.

### 3. memoization and statistical based chosing
The exhaustive state space of valid **answers** is sufficiently small, given there are only 2315 valid answers and the max branching factor
in a directed graph is 3^5 = 243 given each slot can either be G = Green *(exact match)*, Y = Yellow *(letter is exists elsewhere in the word)*, '-' = N/A *(Letter not present*).
This will likely be best served encoded as well, even if it hurts readability a little, given 243 nicely fits into a single
``UByte`` and 2315 fits well within a ``Short``.

**Status: Not yet started** - likely tomorrows project if I can finish cleaning up the rest of the code.
