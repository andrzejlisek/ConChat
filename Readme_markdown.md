# Markdown interpreter

The **ConChat** displays uses the simplified **Markdown** interpreter to display formatted answers and store chat context in single file using the **Markdown** extensions\. The Markdown interpreter is limited, does not support all features, but most answers are readable and displayed correctly\.

The implementation is empiric and based on many real conversations\.

# Working principle

The Markdown interpreter works as state machine and parses the text stream line ly line\. Each line is parsed using the window containing last five characters\. For clarity, not all subtle details are described\.

In the description, the **special character** is every printable character other than letter, number and space\.

# Text line interpretation

The window searchch for text format characters\. These characters can not be preceded by **backslash**, because the backslash instructs the interpreter to write the character as is instead of parsing\.

There is assumed, that beyond line egdes, there are unfinished space character serie\.

## Line analysis before windowing

At the process beginning, if interpreter is not currently in block state, the interpreter checks, if the text line is list item\. List item can begin with one or following:


* Asterisk character\.
* Plus character\.
* Minus character\.
* Number followed by dot character\.

If text line begins from one of above, interpreter detects and remembers the text line indentation \(left margin\) to use for next text lines\.

Then, the text line \(excluding the punctation character if there is list item\) is parsed using the window and current state\.

## Asterisk and underscore

The two characters switches the bond and italic state as following:


* **Single character** \- Switch the **italic state** to opposite\.
* **Double character** \- Switch the **bold state** to opposite\.
* **Triple character** \- Switch the **bold state** and **italic state** to opposite\. Works only if both states are disabled or both states are enabled\.

The characters switches the state or will be printed as are debending on previous and next character\. The asterisk and underscore switches the state under the following conditions:


* Enable state:
  * Previous character is space or special, the next character is letter or special\.
  * Previous character is letter, the next character is letter, works for asterisk only\.
  * Code state is disabled\.
  * Not within the block state\.
* Disable state:
  * Previous character is letter or special, the next character is space or special\.
  * Previous character is letter, the next character is letter, works for asterisk only\.
  * Code state is disabled\.
  * Not within the block state\.

The special character is every character other than space, letter and digit\.

## Single character

The single character can be detected by read last three characters\. To interprets the character as formatting character, the character can not be repeated\.

The following single characters occurence are supported:


* **Underscore** or **asterisk** \- Switch the inline italic state with rules descripbed in **Asterisk and underscore** subchapter\.
* **Grave accent** \- Switch the inline code state when not in block code state\. The inline code state can will be disable even, if finishing **grave accent** is preceded by **backslach** character\.
* **Dollar sign** \- Not parsed in ConChat, this characters switches the equation state\. The support is disable to avoid confusion with the printed prices in dollars\.

## Double character

The double character can be detected by read last four characters\.

The following character sequences are supported:


* **Double underscore** or **double asterisk** \- Switch the inline block state with descripbed in **Asterisk and underscore** subchapter\.
* **Double dollar** \- Switch the equation block state\. The ConChat does not support the LaTexParse, but block equations are displayed as unformatted block text\. The line containing double dollar belongs to the block\.
* **Backslash followed by opening square bracked** \- Works as double dollar\.
* **Backslash followed by closing square bracked** \- Works as double dollar\.
* **Double grave accent** \- Switch the inline code state, which can contain the single grave accent\.

## Triple character

Some triple character sequence can also be interpreted as formatting sequences:


* **Triple underscore** or **triple asterisk** \- Switch the inline bold state and inline italic state simultaneously with rules descripbed in **Asterisk and underscore** subchapter\.
* **Triple grave accent** or **triple tilde** \- Switch the block code state\. The line containing triple grave accent or triple dollar belongs to the block\.

## Line analysis after windowing

After windowing parse, the interpreter checks, if the line is one of the following:


* Header
* Standard splitter
* Technical splitter
* Table

## Header

If the line starts with the **hash** character followed by space, the line is considered as header and is displayed as enlargered font\. The trailing **hash** characters in this case are removed\.

## Standard splitter

The standard splitter is considered, when the text line consists of one of the sequences, without other characters: **underscore**, **asterisk**, **minus**\.

## Technical splitter

The technical splitter is not standard Markdown feature and is implemented for use with **ConChat** only\. Such splitter starts with **triple underline** and ends with **triple unterline**\. Other character between underline determines the line purpose and depends on character next to the unterline sequence:


* **Less\-than sign** character \- The begin of the question message sent to chatbot\. The number is the number of tokens associated with the message and is received from the chatbot\.
* **Greater\-than sign** character \- The begin of the answer message received from chatbot\. The number is the number of tokens associated with the message and is received from the chatbot\.
* **Brackets** \- There is hidden text line and represents the ommision state change to opposite\. Every enabling and disabling message ommision writes this line containing the number of message\.

## Table

The table state will be enabled, when the the text line meets the following conditions:


* Contains **vertical bar** character\.
* Contains the **triple minus** character sequence\.
* Does not contain the alphanumeric character within the same text line\.

Enabling the table state implies conversion the previous text line into table part, assuming the line contains table header\.

In the table state, the first line, which does not contain the **vertical bar**, disables the table state and formats the table\.

## Blank line

The blank line outside the equation state or code state, causes the following:


* Disable inline bold state\.
* Disable inline italic state\.
* Disable inline code state\.
* Reset the indent length\.

# Limitations

The **ConChat** has the following limitations according the standard Markdown specification\.


* The single/double/triple **asterisk** or **underscore** swiches the state immediately, without checking, if the state is disabled later\. In some rare cases, the bold state or italic state can be enabled even, if there is intended to write asterisks/underscore as are\.
* The LaTeX is not supported\. If you don't understand the equation, you should ask the chatbot for the same equation without LaTeX notation or for verbal description\.
* The block equations are printed as unformatted text block by the same way as code text block\.
* The following elements are not supported by **ConChat**, but chatbot may emit these items, which should be readable/usable even without parsing\.
  * Inline equations\. 
  * Website links\.
  * Subscript and superscript text\.\.
  * Strikethrough text\.
* Chatbot probably never emits the following elements and **ConChat** interpretes does not support them:
  * Image links\.
  * Embedded images\.
  * Blockquotes\.
  * Headers defined using **minus sign** or **equal sign** below\.
  * HTML tags\.

Despite the above limitations, the vast majority of conversations are displayed correctly\.




