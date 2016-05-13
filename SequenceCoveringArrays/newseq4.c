#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <time.h>
#define MAXT 10000
#define NTRIALS  1000
int N;    // number of events to generate sequences for
int NSEQ; // number of sequences = N(N-1)(N-2)
int reversal;	// toggle for whether to generate new tests by reversing just created one 

int ****chk;		//int chk[N][N][N];	
int **test;		// int test[MAXT][N];
int **seq;		// int seq[NSEQ][3]
int **tmptest;		// int test[MAXT][N];

int i,j,k,m,n,q,r;
int nt=0, cnt=0;
int fnd;
int tp;
int all_covered=0;
int bestidx, bestcov;
int i1, j1, k1, r1;

main(int argc, char *argv[]) 
{
void analyze();
int used();
void prtest();

//===========================================================================
// set up using input args
if (argc < 1) {
    fprintf(stderr,"Usage: <number of events> \n");
    return 1;
}
N = atoi(argv[1]);
NSEQ = N*(N-1)*(N-2)*(N-3);

// allocate space for check array
chk = (int ****)malloc(sizeof(int ***) * N);
for (i=0; i<N; i++) {
	chk[i] = (int ***)malloc(sizeof(int **) * N);
		for (j=0; j<N; j++) {
			chk[i][j] = (int **)malloc(sizeof(int*) * N);
			for (k=0; k<N; k++) {
				chk[i][j][k] = (int *)malloc(sizeof(int) * N);
}}}

test = (int **)malloc(sizeof(int *) * MAXT );
for (i=0; i<MAXT; i++) {test[i] = (int *)malloc(sizeof(int) * N); }

seq = (int **)malloc(sizeof(int *) * NSEQ );
for (i=0; i<NSEQ; i++) {seq[i] = (int *)malloc(sizeof(int) * 3); }
// set up whether to create tests by reversing generated ones;  add user input for this option later
reversal = (N > 5? 1 : 0);


tmptest = (int **)malloc(sizeof(int *) * MAXT );
for (i=0; i<NTRIALS; i++) {tmptest[i] = (int *)malloc(sizeof(int) * N); }
srand(time(0));

//===========================================================================
printf("Generating test sequences for %d events\n", N);
// initialize sequence array w/ all 3-seq
// clear checks
m=0; 
while (m<NSEQ) {
   for (i=N-1; i>=0; i--) {
   for (j=N-1; j>=0; j--) {
   for (k=N-1; k>=0; k--) {
   for (r=N-1; r>=0; r--) {
      if (i != j && i != k && j != k && i != r && j != r && k != r ) 
	  {seq[m][0]=i; seq[m][1]=j; seq[m][2]=k; seq[m][3]=r;   m++;  chk[i][j][k][r] = 0;}
      }}}}
}

//==============================================
// init tests with all 2-seq
for (i=0; i<N; i++) {test[0][i] = i; test[1][i] = N-1-i; }
nt=2;
analyze(nt,0);  // check off covered seqs

//=========================================================================================
while ( !allcovered() && nt < MAXT) {
	// create candidate tests, see which one improves coverage most       
	for (i=0; i<NTRIALS; i++) {  // generate NTRIALS candidates
		tmptest[i][0] = rand()%N;
		for (j=1; j<N; j++) {
			n=rand()%N; while (tmpused(i,n,j)) n=rand()%N;
			tmptest[i][j] = n;   
		}       
	}
	// pick the best one
	bestidx=0; bestcov=0;   
	for (m=0; m<NTRIALS; m++) {
		cnt=0;  
		for (i=0;   i<N-3; i++) {
		for (j=i+1; j<N-2; j++) {
		for (k=j+1; k<N-1; k++) {
		for (r=k+1; r<N;   r++) {
			i1= tmptest[m][i]; j1= tmptest[m][j];  k1= tmptest[m][k];  r1= tmptest[m][r];
			if ( chk[i1][j1][k1][r1] == 0  && i != j && i != k && j != k && i != r && j != r && k != r ) { cnt++; }
		}}}}	
	if (cnt > bestcov) {bestcov=cnt; bestidx=m; }
	}	// printf("  best %d   bidx %d    \n", bestcov,bestidx);
	for (i=0; i<N; i++) test[nt][i] = tmptest[bestidx][i];
	nt++; // bump test count
	analyze(nt,0);  // check off covered seqs


	if (reversal) {
		// A new test has been created.  Now reverse it to create another one.
		for (i=0; i<N; i++) test[nt][i] = test[nt-1][N-1-i];
		nt++;
		analyze(nt,0);  // check off covered seqs
	}

}  // end while  !allcovered
//========================================================================================

// print tests
printf("==== %d TESTS ====\n", nt);
for (m=0; m<nt; m++) {
   for (j=0; j<N; j++) {fprintf(stderr,"%d,", test[m][j]); }
   fprintf(stderr,"\n");
   }

//analyze(nt,tp);  // check off covered seqs

// count covered seqs
cnt=0;
// fprintf(stderr, "\nCovered \n");
   for (i=0; i<N; i++) {
   for (j=0; j<N; j++) {
   for (k=0; k<N; k++) {
   for (r=0; r<N; r++) {
		if ( chk[i][j][k][r] == 1  && i != j && i != k && j != k && i != r && j != r && k != r ) 
			{cnt++;  
			//fprintf(stderr, "%d %d %d\t", i,j,k); 
			}

}}}}		
printf("Tests:  %d.  Seqs covered: %d/NSEQ: %d  = %f \n", nt, cnt, NSEQ, (float)cnt/(float)NSEQ );
}   // end main

//========================================================================================
int tmpused(ti, digit, len) 
int ti; // index into test array
int digit;  // digit to check
int len; // length of test to check

{
int i,j;
for (i=0; i<len; i++) if (tmptest[ti][i] == digit) return 1;
return 0;

}
//========================================================================================
int used(ti, digit, len) 
int ti; // index into test array
int digit;  // digit to check
int len; // length of test to check

{
int i,j;
for (i=0; i<len; i++) if (test[ti][i] == digit) return 1;
return 0;

}
//========================================================================================
void analyze(tst, len) 
int tst;  // last complete test in test array
int len;  // len of tst+1 written so far
{
// analyze tests
// for each 3-seq in tests, set chk 
int m;
for (m=0; m<tst; m++) {
   for (i=0; i<N-2; i++) {
   for (j=i+1; j<N-1; j++) {
   for (k=j+1; k<N; k++) { 
   for (r=k+1; r<N; r++) {
   {
      chk[test[m][i]][test[m][j]][test[m][k]][test[m][r]] = 1; 
	  // fprintf(stderr, "marking %d %d %d\n", test[m][i],  test[m][j] , test[m][k] );
   }
}}}}
}
// now mark partially complete test
   for (i=0; i<len-3; i++) {
   for (j=i+1; j<len-2; j++) {
   for (k=j+1; k<len-1; k++) {
   for (r=k+1; r<len; r++) {   
   {
      chk[test[m][i]][test[m][j]][test[m][k]][test[m][r]] = 1; 
	  // fprintf(stderr, "marking %d %d %d\n", test[m][i],  test[m][j] , test[m][k] );
   }
}}}}
}
//========================================================================================
void prtest(tst,len)
int tst;
int len;
{
int q;
	fprintf(stderr,"Adding: ");
	for (q=0; q<len; q++) fprintf(stderr, "%d ", test[tst][q] );
	fprintf(stderr, "\n");
}
//========================================================================================
int allcovered()
{
int i,j,k,r;
int cnt=0;

for (i=0; i<N; i++) {
   for (j=0; j<N; j++) {
   for (k=0; k<N; k++) {
   for (r=0; r<N; r++) {
		if ( chk[i][j][k][r] == 1  && i != j && i != k && j != k && i != r && j != r && k != r ) {cnt++; }
		}}}}
if (cnt < NSEQ) return 0;
return 1;
}

