/* Version without the PAPI library to be able to compile on Windows */
#include <stdio.h>
#include <iostream>
#include <fstream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <string.h>
#include <algorithm>


using namespace std;

#define SYSTEMTIME clock_t

void matrix_print(double *mat, int N)
{
	cout << "Result matrix: " << endl;
	for (int i = 0; i < N ; i++)
	{
		for (int j = 0; j < N; j++)
			cout << mat[i * N + j] << " ";
		cout << endl;
	}
	cout << endl;
}

void OnMult(int m_ar, int m_br)
{

	SYSTEMTIME Time1, Time2;

	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;

	pha = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phb = (double *)malloc((m_ar * m_ar) * sizeof(double));
	phc = (double *)malloc((m_ar * m_ar) * sizeof(double));

	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
			pha[i * m_ar + j] = (double)1.0;

	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phb[i * m_br + j] = (double)(i + 1);

	Time1 = clock();

	for (i = 0; i < m_ar; i++)
	{
		for (j = 0; j < m_br; j++)
		{
			temp = 0;
			for (k = 0; k < m_ar; k++)
			{
				temp += pha[i * m_ar + k] * phb[k * m_br + j];
			}
			phc[i * m_ar + j] = temp;
		}
	}

	Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	matrix_print(phc, min(m_ar, 10));


	free(pha);
	free(phb);
	free(phc);
}

// add code here for line x line matriz multiplication
void OnMultLine(int m_ar, int m_br)
{
	SYSTEMTIME Time1, Time2;

	int mSize = m_ar * m_br;
	int n = m_ar;

	char st[100];
	double temp;
	int i, j, k;

	double *pha, *phb, *phc;

	// allocate memory for the matrices and for the result matrix
	pha = (double *)malloc(mSize * sizeof(double));
	phb = (double *)malloc(mSize * sizeof(double));
	phc = (double *)malloc(mSize * sizeof(double));

	// initialize the first matrix with 1.0
	for (i = 0; i < m_ar; i++)
		for (j = 0; j < m_ar; j++)
			pha[i * m_ar + j] = (double)1.0;

	// initialize the second matrix values of the form i+1 where i is the line number
	for (i = 0; i < m_br; i++)
		for (j = 0; j < m_br; j++)
			phb[i * m_br + j] = (double)(i + 1);

	// start the timer
	Time1 = clock();

	/* line x line matrix multiplication */
	// For each line of the first matrix
	for (i = 0; i < n; i++)
	{
		// for each column of the second matrix
		for (k = 0; k < n; k++)
		{
			temp = pha[i * n + k];
			// for each element of the line
			for (j = 0; j < n; j++)
			{
				// multiply the elements and add to the result matrix
				phc[i * n + j] += temp * phb[k * n + j];
			}
		}
	}

	// stop the timer
	Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	// display 10 elements of the result matrix tto verify correctness
	matrix_print(phc, min(n, 10));

	free(pha);
	free(phb);
	free(phc);
}

// add code here for block x block matriz multiplication
// add code here for block x block matriz multiplication
void OnMultBlock(int NA, int NB, int bkSize)
{
	SYSTEMTIME Time1, Time2;

	int mSize = NA * NB;
	int N = NA;

	char st[100];
	double temp;
	int y, x;

	double *A, *B, *C;

	Time1 = clock();

	// allocate memory for the matrices and for the result matrix
	A = (double *)malloc(mSize * sizeof(double));
	B = (double *)malloc(mSize * sizeof(double));
	C = (double *)malloc(mSize * sizeof(double));

	memset(C, 0, mSize * sizeof(double));

	// initialize the first matrix with 1.0
	for (y = 0; y < NA; y++)
		for (x = 0; x < NA; x++)
			A[y * NA + x] = (double)1.0;

	// initialize the second matrix values of the form i+1 where i is the line number
	for (y = 0; y < NB; y++)
		for (x = 0; x < NB; x++)
			B[y * NB + x] = (double)(y + 1);

	int by, bx, k, kk;
	for (by = 0; by < N; by += bkSize)
	{
		cout << by << endl;
        for (bx = 0; bx < N; bx += bkSize)
		{
            for (k = 0; k < N; k += bkSize)
			{
				int	ymax = min(by + bkSize, N);
				int	xmax = min(bx + bkSize, N);
				int	max = min(k + bkSize, N);
                for (y = by; y < ymax; y++)
                    for (x = bx; x < xmax; x++)
                        for (kk = k; kk < max; kk++)
                        	C[y * N + kk] += A[y * N + x] * B[x * N + kk];
			}
		}
	}


	Time2 = clock();
	sprintf(st, "Time: %3.3f seconds\n", (double)(Time2 - Time1) / CLOCKS_PER_SEC);
	cout << st;

	matrix_print(C, min(N, 10));

	free(A);
	free(B);
	free(C);
}


int main(int argc, char *argv[])
{

	char c;
	int lin, col, blockSize;
	int op;

	op = 1;
	do
	{
		cout << endl
			 << "0. Exit" << endl;
		cout << "1. Multiplication" << endl;
		cout << "2. Line Multiplication" << endl;
		cout << "3. Block Multiplication" << endl;
		cout << "Selection?: ";
		cin >> op;
		if (op == 0)
			break;
		printf("Dimensions: lins=cols ? ");
		cin >> lin;
		col = lin;

		switch (op)
		{
		case 1:
			OnMult(lin, col);
			break;
		case 2:
			OnMultLine(lin, col);
			break;
		case 3:
			cout << "Block Size? ";
			cin >> blockSize;
			OnMultBlock(lin, col, blockSize);
			break;
		}

	} while (op != 0);

	return 0;
}
