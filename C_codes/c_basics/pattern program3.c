/*
 
 		pattern program
****
$$$$
****
$$$$
       
*/


#include<stdio.h>
int main()
{
	int i,j,k;

	for(i=1; i<=4; i++)
	{
		for(j=1; j<=4; j++)
		{
			if(i%2!=0)
			{
				printf("*");
			}
			else
			{
				printf("$");
			}
		}

		printf("\n");
	}



	return 0;
}
