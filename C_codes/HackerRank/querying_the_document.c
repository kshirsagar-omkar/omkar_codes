/*  
A document is represented as a collection paragraphs, a paragraph is represented as a collection of sentences, a sentence is represented as a collection of words and a word is represented as a collection of lower-case ([a-z]) and upper-case ([A-Z]) English characters.

You will convert a raw text document into its component paragraphs, sentences and words. To test your results, queries will ask you to return a specific paragraph, sentence or word as described below.

Alicia is studying the C programming language at the University of Dunkirk and she represents the words, sentences, paragraphs, and documents using pointers:

A word is described by char *.
A sentence is described by char**. The words in the sentence are separated by one space (" "). The last word does not end with a space(" ").
A paragraph is described by char*** . The sentences in the paragraph are separated by one period (".").
A document is described by char**** . The paragraphs in the document are separated by one newline("\n"). The last paragraph does not end with a newline.


	for example : 
	Learning C is fun.
	Learning pointers is more fun.It is good to have pointers.
	
	
		
	
	The only sentence in the first paragraph could be represented as:
	char** first_sentence_in_first_paragraph = {"Learning", "C", "is", "fun"};
	The first paragraph itself could be represented as:
	char*** first_paragraph = {{"Learning", "C", "is", "fun"}};
	The first sentence in the second paragraph could be represented as:
	char** first_sentence_in_second_paragraph = {"Learning", "pointers", "is", "more", "fun"};
	The second sentence in the second paragraph could be represented as:
	char** second_sentence_in_second_paragraph = {"It", "is", "good", "to", "have", "pointers"};
	The second paragraph could be represented as:
	char*** second_paragraph = {{"Learning", "pointers", "is", "more", "fun"}, {"It", "is", "good", "to", "have", "pointers"}};
	Finally, the document could be represented as:
	char**** document = {{{"Learning", "C", "is", "fun"}}, {{"Learning", "pointers", "is", "more", "fun"}, {"It", "is", "good", "to", "have", "pointers"}}};



	Alicia has sent a document to her friend Teodora as a string of characters, i.e. represented by char* not
	char****. Help her convert the document to char**** form by completing the following functions:
	
	char**** get_document(char* text)  to return the document represented by char****.
	char*** kth_paragraph(char**** document, int k) to return the kth paragraph.
	char** kth_sentence_in_mth_paragraph(char**** document, int k, int m) to return the kth sentence in the mth paragraph.
	char* kth_word_in_mth_sentence_of_nth_paragraph(char**** document, int k, int m, int n) to return the kth word in the mth sentence of the nth paragraph.
	
	Input Format

	The first line contains the integer paragraph_count.
	Each of the next paragraph_count lines contains a paragraph as a single string.
	The next line contains the integer q, the number of queries.
	Each of the next q lines or groups of lines contains a query in one of the following formats:
	
	
	1 The first line contains 1 k :

	The next line contains an integer x, the number of sentences in the kth paragraph.
	Each of the next  lines contains an integer a[i], the number of words in the ith sentence.
	This query corresponds to calling the function kth_paragraph.
	
	2 The first line contains 2 k m:

	The next line contains an integer x, the number of words in the kth sentence of the mth paragraph.
	This query corresponds to calling the function kth_sentence_in_mth_paragraph.
	
	3 The only line contains 3 k m n:

	This query corresponds to calling the function 
	kth_word_in_mth_sentence_of_nth_paragraph
	
	
	

	Constraints

	The text which is passed to the get_document has words separated by a space (" "), sentences separated by a period (".") and paragraphs separated by a newline("\n").
	The last word in a sentence does not end with a space.
	The last paragraph does not end with a newline.
	The words contain only upper-case and lower-case English letters.
	1 <= number of characters in the entire document <= 1000 
	1 <= number of paragraphs in the entire document <= 5




	Output Format

	Print the paragraph, sentence or the word corresponding to the query to check the logic of your code.

	Sample Input 0

	2
	Learning C is fun.
	Learning pointers is more fun.It is good to have pointers.
	3
	1 2
	2
	5
	6
	2 1 1
	4
	3 1 1 1
	Sample Output 0

	Learning pointers is more fun.It is good to have pointers.
	Learning C is fun
	Learning
	Explanation 0

	The first query corresponds to returning the second paragraph with 2 sentences of lengths 5 and 6 words.
	The second query correspond to returning the first sentence of the first paragraph. It contains 4 words.
	The third query corresponds to returning the first word of the first sentence of the first paragraph.


code =>

		#include <stdio.h>
		#include <stdlib.h>
		#include <string.h>
		#include<assert.h>
		#define MAX_CHARACTERS 1005
		#define MAX_PARAGRAPHS 5

		char* kth_word_in_mth_sentence_of_nth_paragraph(char**** document, int k, int m, int n) {

		}

		char** kth_sentence_in_mth_paragraph(char**** document, int k, int m) { 

		}

		char*** kth_paragraph(char**** document, int k) {

		}

		char**** get_document(char* text) {

		}


		char* get_input_text() {	
		    int paragraph_count;
		    scanf("%d", &paragraph_count);

		    char p[MAX_PARAGRAPHS][MAX_CHARACTERS], doc[MAX_CHARACTERS];
		    memset(doc, 0, sizeof(doc));
		    getchar();
		    for (int i = 0; i < paragraph_count; i++) {
			scanf("%[^\n]%*c", p[i]);
			strcat(doc, p[i]);
			if (i != paragraph_count - 1)
			    strcat(doc, "\n");
		    }

		    char* returnDoc = (char*)malloc((strlen (doc)+1) * (sizeof(char)));
		    strcpy(returnDoc, doc);
		    return returnDoc;
		}

		void print_word(char* word) {
		    printf("%s", word);
		}

		void print_sentence(char** sentence) {
		    int word_count;
		    scanf("%d", &word_count);
		    for(int i = 0; i < word_count; i++){
			printf("%s", sentence[i]);
			if( i != word_count - 1)
			    printf(" ");
		    }
		} 

		void print_paragraph(char*** paragraph) {
		    int sentence_count;
		    scanf("%d", &sentence_count);
		    for (int i = 0; i < sentence_count; i++) {
			print_sentence(*(paragraph + i));
			printf(".");
		    }
		}

		int main() 
		{
		    char* text = get_input_text();
		    char**** document = get_document(text);

		    int q;
		    scanf("%d", &q);

		    while (q--) {
			int type;
			scanf("%d", &type);

			if (type == 3){
			    int k, m, n;
			    scanf("%d %d %d", &k, &m, &n);
			    char* word = kth_word_in_mth_sentence_of_nth_paragraph(document, k, m, n);
			    print_word(word);
			}

			else if (type == 2){
			    int k, m;
			    scanf("%d %d", &k, &m);
			    char** sentence = kth_sentence_in_mth_paragraph(document, k, m);
			    print_sentence(sentence);
			}

			else{
			    int k;
			    scanf("%d", &k);
			    char*** paragraph = kth_paragraph(document, k);
			    print_paragraph(paragraph);
			}
			printf("\n");
		    }     
		}

*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include<assert.h>
#define MAX_CHARACTERS 1005
#define MAX_PARAGRAPHS 5

char* kth_word_in_mth_sentence_of_nth_paragraph(char**** document, int k, int m, int n) {
    return document[n - 1][m - 1][k - 1];
}

char** kth_sentence_in_mth_paragraph(char**** document, int k, int m) {
    return document[m - 1][k - 1];
}

char*** kth_paragraph(char**** document, int k) {
    return document[k - 1];
}

char**** get_document(char* text) {
    char**** document;
    char* token;
    char* paragraph_tokens[MAX_PARAGRAPHS];
    int paragraph_count = 0, sentence_count, word_count;
    
    document = (char****)malloc(MAX_PARAGRAPHS * sizeof(char***));
    
    // Split the text into paragraphs
    token = strtok(text, "\n");
    while (token != NULL) {
        paragraph_tokens[paragraph_count] = token;
        paragraph_count++;
        token = strtok(NULL, "\n");
    }

    // Split each paragraph into sentences and words
    for (int i = 0; i < paragraph_count; i++) {
        char* sentence_tokens[MAX_CHARACTERS];
        char* sentence = paragraph_tokens[i];
        int sentence_count = 0;

        token = strtok(sentence, ".");
        while (token != NULL) {
            sentence_tokens[sentence_count] = token;
            sentence_count++;
            token = strtok(NULL, ".");
        }

        document[i] = (char***)malloc(sentence_count * sizeof(char**));

        for (int j = 0; j < sentence_count; j++) {
            char* word_tokens[MAX_CHARACTERS];
            char* word = sentence_tokens[j];
            int word_count = 0;

            token = strtok(word, " ");
            while (token != NULL) {
                word_tokens[word_count] = token;
                word_count++;
                token = strtok(NULL, " ");
            }

            document[i][j] = (char**)malloc(word_count * sizeof(char*));

            for (int k = 0; k < word_count; k++) {
                document[i][j][k] = word_tokens[k];
            }
        }
    }

    return document;
}


char* get_input_text() {	
    int paragraph_count;
    scanf("%d", &paragraph_count);

    char p[MAX_PARAGRAPHS][MAX_CHARACTERS], doc[MAX_CHARACTERS];
    memset(doc, 0, sizeof(doc));
    getchar();
    for (int i = 0; i < paragraph_count; i++) {
        scanf("%[^\n]%*c", p[i]);
        strcat(doc, p[i]);
        if (i != paragraph_count - 1)
            strcat(doc, "\n");
    }

    char* returnDoc = (char*)malloc((strlen (doc)+1) * (sizeof(char)));
    strcpy(returnDoc, doc);
    return returnDoc;
}

void print_word(char* word) {
    printf("%s", word);
}

void print_sentence(char** sentence) {
    int word_count;
    scanf("%d", &word_count);
    for(int i = 0; i < word_count; i++){
        printf("%s", sentence[i]);
        if( i != word_count - 1)
            printf(" ");
    }
} 

void print_paragraph(char*** paragraph) {
    int sentence_count;
    scanf("%d", &sentence_count);
    for (int i = 0; i < sentence_count; i++) {
        print_sentence(*(paragraph + i));
        printf(".");
    }
}

int main() 
{
    char* text = get_input_text();
    char**** document = get_document(text);

    int q;
    scanf("%d", &q);

    while (q--) {
        int type;
        scanf("%d", &type);

        if (type == 3){
            int k, m, n;
            scanf("%d %d %d", &k, &m, &n);
            char* word = kth_word_in_mth_sentence_of_nth_paragraph(document, k, m, n);
            print_word(word);
        }

        else if (type == 2){
            int k, m;
            scanf("%d %d", &k, &m);
            char** sentence = kth_sentence_in_mth_paragraph(document, k, m);
            print_sentence(sentence);
        }

        else{
            int k;
            scanf("%d", &k);
            char*** paragraph = kth_paragraph(document, k);
            print_paragraph(paragraph);
        }
        printf("\n");
    }     
}



















