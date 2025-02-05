# ConChat overview

**ConChat** is the simple console application written in Java language and the main purpose is using with **OpenAI ChatGPT** and **Google Gemini** services\. The application works natively on Linux and probably will work on every Unix\-like system including MacOs X, but is not tested\. On Windows the application will not work due to Java console support limitations\. Application should work in every operating system, when standard streams are redirected to VT100\-compatible terminal\.

**ConChat** supports both **ChatGPT** and **Gemini** chatbot service through official REST API\. These APIs is payable and requires indivual API key for use\. In order to use the application, the one of the two services is sufficient, but access to both services gives more LLM engines to use\.

# Configuration file

Before the first use, you have to set the API key in the configuration file and at the time, you can configure the other work aspects\.

The configuration file **config\.txt** has the following options, the italic options can be changed during application session:


* **KeyGpt** \- API key for OpenAI ChatGPT\.
* **KeyGemini** \- API key for Google Gemini\.
* **Favorite** \- Model favorite list separated with semicolon\.
* ***Model*** \- Currently selected model name\.
* ***FieldSize*** \- Minimum field size for input text in text lines\. The real current field size depends on current console resolution and can be increased by 1\.
* ***Temperature*** \- Probability usage by chatbot from **0** to **200** called as **temperature**\.
  * The **0** value causes almost deterministic chatbot working\.
  * Some value greater than **200** is allowed, but means, that temperature is not provided and the chatbot will use default temperature\.
* ***TopP*** \- The **nucleus sampling** token probability threshold from **0** to **100**\.
  * The **0** value causes almost deterministic chatbot working\.
  * Some value greater than **100** is allowed, but means, that nucleus sampling is not provided and the chatbot will use default temperature\.
* **TopK** \- The word set size limit for answer generation, the parameter is used for **Google Gemini** only\. The value can be:
  * **0** \- Parameter is not used\.
  * **From 1 to unlimited high number** \- Parameter is used with value\. The 1 value may cause chaotic answer generation while used high **Temperature** and high **TopP**\.
* **PresencePenalty** and **FrequencyPenalty** \- Obstructing token repeat in generated answer based on token presence and frequency respectively\. The value is from **0** to **100**\.
  * The **0** means no obstruction \(no penalty\)\.
  * Some value greater than **100** is allowed, but means, that this parameter is not provided and the chatbot will use default obstructions\.
* ***HistoryTokens*** \- Maximum number of history tokens contained in last history messages \(questions and answers\) sent to chatbot everytime you writes the question\. The **0** value means unlimited history size, but chatbot engine may have own limit\.
* ***AnswerTokens*** \- Maximum number of answer tokens\. Limited number may limit potential usage cost, but may cause incomplete answer\. The **0** values means unlimited answer size\.
* ***WaitTimeout*** \- Waiting for answer timeout in seconds\.
* ***CellWidth*** \- Minimum cell width in table, when the answer contains any **Markown** formatted table\. There is the size used for maximum space size for word wrapping\.
* ***Log*** \- The **http\.txt** log file option:
  * **0** \- Do not create the log\.
  * **1** \- Append every request and response to log file, do not clear the file\.
  * **2** \- Append every request and response to log file, clear the log file at the **ConChat** startup\.
* ***Context*** \- Number of startup chat context\. **ConChat** contains 10 chat contexts numbered from 0 to 9\.
* ***MarkdownHeader*** \- The first header level, which will be indicated by smaller font\.
* ***MarkdownDuospace*** \- Measure the non\-ASCII characters in order to propertly display using the duo space fonts in terminal:
  * **0** \- Assume, thal all characters are the single cell width\. Use this in the following cases:
    * Conversations contains the ASCII characters and other single width characters only, like latin\-based characters\.
    * The terminal application uses monospace font\.
    * There are problems with character width measurement\.
  * **1** \- Measure non\-ASCII characters\. Use if the conversations contains double\-width characters, like CJK characters i duospace fonts\. Every non\-ASCII character wil be measured\.
* **TestModel** \- Test message to test model validity to text chat while creating model list\. If the text is blank, the test is not performed\.

# ConChat run

In order to run ConChat, you have to run the **ConChat\.jar** file in JVM with directory provided as parameter\. For instance, the command with OpenJDK may be as following:

```
java -jar ConChat.jar /mount/disk/conchat/
```

The directory should contain the **config\.txt** file and will be use for save contexts\.

If you not provide the directory, there will be used the default directory\. The execution command may looks like following:

```
java -jar ConChat.jar
```

If the directory does not contain the **config\.txt** file or this file does not contain any of the API key, the **ConChat** will not run and will print the work directory, which should contain the **config\.txt** file\.

In this repository, there is **StartConChat\.sh** file, starts the **ConChat** in Linux\.

# ConChat usage

The GUI consists of two parts\. The top, larger part, is the output screen, the bottom, smaller part is the input text field\. The application has two possible states:


* **Operation state** \- The default state, where you can ask the chatbot for something and you will get the answer\.
* **Configuration state** \- The alternative state, which allow to change settings\.

## Keys and commands in both states

In both states, everytime you can press these keys:


* **Enter** \- Depends on input text, command words are case insensitive:
  * **Blank field** \- Switch between the two states\.
  * **"clear"** \- Clear the current context \(conversation\)\.
  * **"exit"** \- Exit from **ConChat**\.
  * **"repaint"** \- Repaint the interface after resize\. This command also clears all contextes and reloads them from files\. Use the command even if you change the font typeface in console/terminal application when the contexts contains special characters, which can be double width, like CJK characters\.
  * **"copy"** \- Copy the current message \(in the middle of the screen\) to edit field\. Then, you can edit this question before send\.
  * **"counterreset"** \- Reset the tonen counter for the currently selected model\.
  * **"archive"** \- Archive the current context to file\. The physical file will by named by **contextXYZ\.txt**, where XYZ is current date/time stamp written by 14 digits\.
  * **"archdelete"** \- Delete last created or restored archive, which is highlighted\.
  * **"markdowntest"** \- Switch into the raw text display instead of the **Markdown** text\. Send one more time for returning switch into standard display\. Usable for debugging **Markdown** display glitches\.
  * **"markdownheader?"** \- Use the digit from **1** to **7** instead of **?** character\. Changes the header display threshold as following examples:
    * **"markdownheader1"** \- Displays all header levels using smaller font\.
    * **"markdownheader2"** \- Displays the first leves using larger font and other header levels using smaller font\.
    * **"markdownheader6"** \- Displays the first five leves using larger font and last header level using smaller font\.
    * **"markdownheader7"** \- Displays all header levels using larger font\.
  * **"markdownduospace0"** \- Disable duospace font while Markdown display\. Use, when the console uses monospace font and all characters are assumed, that covers single cell\.
  * **"markdownduospace1"** \- Enable duospace font while Markdown display\. Use, when the console uses duospace font and non\-ASCII characters will be measured\.
  * **Other text** \- Depends on state and text, described in below subchapters\. 
* **Up Arrow**, **Down Arrow**, **Page Up**, **Page Down**, **Home**, **End** \- Scroll the text in answer screen\.
* **Left Arrow**, **Right Arrow** \- Move cursor across input text\.
* **Esc** pressed twice \- Clear the text field\.

## Operation state

The default state is the operation state\. In this state, you can talk with the chatbot\. In this state, there are 10 independed contexts \(conversations\)\. In order to change the context, input **single digit** end press **Enter**\. The context has number from **0** to **9**\. Otherwise, and if there is not command word, the text will be sent into the chatbot server and answer will be displayed after receiving\. During waiting for answer, you can not input something\.

The answer can contain unformatted text blocks according the **Mardown** code\. If the text block is wider than the screen width, you can scroll the block horizontally by pressing the **Left Arrow** and the **Right Arrow** keys after place the block in the middle of output screen\.

The conversation are stored in the files named as **context0\.txt**, **context1\.txt** and so on, these files are **Markdown** files and can be viewed in ordinary Markdown viewer/interpreter\. The **clear** command clears the screen and removes the file\. **ConChat** has simplified and limited **Markdown** support described in the **Readme\_markdown\.md** file and seldom answers may be displayed incorrectly\.

The chatbot actually works as stateless machine, so everytime, if you send further questions, the whole conversation history \(excluding the messages with 0 tokens\) is sent to server\. You can ommit any message from history by move into the middle of the screen and press the **Tab** key\. Ommited messages are indicated by strikethrough\. You can turn ommision off by pressing the **Tab** key one more within the same message\.

The messages, which are ommited due to **0** tokens length or exceeding history tokens limit are indicated by strikethrough line at the screen edges\.

In order to check the message size in tokens, you can press the **Insert** or **F12** key\. The message information will be written into the input text field:


* Message length in tokens if the length is saved\.
* The model name if the model differs than the current model, the model name will also be written\.

The information will remind, which model you was used within the current conversations\. If toy get the lessage length only, the current model is the same as used in the talk\.

Everytime, if you send the question, the chat server sends the number of tokens for question and answer\. These numbers are stored in the context and erroneous messages including questions without answer, are not sent to server\. These messages are indicated by strikethrough mark in the first and last text column and will be permanently ommited\.

You cam modify the conversation outside the **ConChat**\. Simply, modify the file associaded to the context, for instance, the **context3\.txt** file for context **3**\. After modification, restart the **ConChat** or rexecure resize command by write **resize** word and press **Enter** key\.

## Configuration state

The **Operation state** can be switched to **Configuration state** by pressing **Enter** key without writing anything\. Pressing **Enter** key one more time, returns to **Operational state**\.

In this stare, there are displayed following information:


* Number of current context\.
* Number of messages and tokens contained in current context, every number consists of two parts:
  * **Before slash** \- The number of tokens/messages, which will be sent to the chatbot when you send next question\. The manual message ommision and history token limit affects the number\.
  * **After slash** \- The number of all tokens/messages in this context\.
* Word commands, which works in both states\.
* The configuration parameter and one\-letter commands for change this parameters\.
* List of favorite models, which are available and included in **Favorite** parameter in **config\.txt** file\. These models are numbered for ease change\.
* List of archive context files treated as further items on favorite model list\.
* List of available models with token counter\. These models are in **models\.txt** file\. The counter consists of three numbers:
  * **History** token counter
  * **Question** token counter
  * **Answer** token counter

The one\-letter commands consists of single letter \(case insensitive\) and one number\. For instance, in order to set the temperature to **100**, input **t100** and press **Enter**\. In order to set waiting timeout to one minute \(60 seconds\), input **w60** and press Enter\. The value will be automatically updated\.

There are two ways to change the current model or restore archive context:


* Input the item number on favorite list and press **Enter**\.
* Input the model name or archive context name and press **Enter**\.

The currently selected model is highlighted\. The archive context will be highlighted after restored\.

## Duospace fonts

The current version of **ConChat** supports duospace fonts in answer display and **Markdown** parse\. The actual duospace characters are not arbitrally defined and depends on font used in the text terminal\. You can switch the duospace font support by **MarkdownDuospace** parameter in **config\.txt** or by using **markdownduospace0** or **markdownduospace1** commands\.

**ConChat** while duospace font is enabled, assumes, that all ASCII characters covers single cell\. Some non\-ASCII characters can cover two cells instead of one cell\. The such character occurs in the context file, the **ConChat** measures the character width by writin in the screen and getting the cursor position\. Every character is measuded once until close application or perform the **repaint** command\.

There is the reason, why the application start up may take a while, if the context files contains the non\-ASCII characters\.

For instance, assume, that the the conversation contains the following text:

```
Czarna krowa w kropki bordo
gryz**ł**a traw**ę** kr**ę**c**ą**c mord**ą**.
Kr**ę**c**ą**c mord**ą** i rogami
gryz**ł**a traw**ę** wraz z jaskrami.
```

The bolded character will be measured at the first occurence after application startup or last **repaint** command\.\.

# Available model list

Both **OpenAI ChatGPT** and **Google Gemini** provides several models\. The model list are stored in the **models\.txt** file\. You can edit or update the model list\.

If you want to edit the model silt, siply open the file in the plain text editor\. Every model is in separated line and is preceeded with one of the characters:


* 1 \- Model to use with **OpenAI ChatGPT**\.
* 2 \- Model to use with **Google Gemini**\.

After editing the file, you hane to re\-run the **ConChat**\.

In order to automatically update, close the **ConChat** application, remove the **models\.txt** file and run the apllication\. At the startup, the aplication will download the model list\. If the **KeyGpt** or **KeyGemini** parameter in **config\.txt** is not blank, **ConChat** assumes, that you use this service and tries to download model list\. If there is error due to incorrect API key, in the model list, there will be dummy models with **error** word in name and raw error message\. In this case, the model list will not stored\.

There are two scenarios depending on the **TestModel** parameter in **config\.txt** file:


* **TestModel** is blank \- Every model from the downloaded list will be stored in **models\.txt** and available to select in **Configuration state** even, if the model is not purposed to work with chatbot\.
* **TestModel** is not blank \- For each moder, there will be send the question provided as **TextModel** parameter\. The model will be considered as valid if the chatbot server give answet for questions\. The answer will not analyzed\. If there is error answer, the model will not inslude and it will be sured, that the list contains usable models only instead of all theoretically available models\. The procedure may take several minutes and consumed tokens will not encountered in token counters\.

# Archive context files

In the favorite model list, there is list of archive context file\. The item name consists of date and time presented as 14 digits of date \(yyyymmdd\) and time\(hhmmss\)\.

If you want to restore the archive to currently selected context, write the item name or item number and press Enter\. The selected context will be highlighted\.

Directly after archive \(command **archive** in operation state\), the last created archive will be highlighted\.

The highlighted archive context will be deleted by **archdelete** command\. If not any context is highlighted, the **archdelete** command will do nothing\.

The archive context can be restore to another context number, so the archive and restore can be also used to copy one context to another context\.




