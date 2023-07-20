#include <stdio.h>
#include <string.h>
#include <iostream>
#include <iomanip>
#include <time.h>
#include <cstdlib>
#include <papi.h>

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

void handle_error(int retval)
{
	printf("PAPI error %d: %s\n", retval, PAPI_strerror(retval));
	exit(1);
}

void init_papi()
{
	int retval = PAPI_library_init(PAPI_VER_CURRENT);
	if (retval != PAPI_VER_CURRENT && retval < 0)
	{
		printf("PAPI library version mismatch!\n");
		exit(1);
	}
	if (retval < 0)
		handle_error(retval);

	std::cout << "PAPI Version Number: MAJOR: " << PAPI_VERSION_MAJOR(retval)
			  << " MINOR: " << PAPI_VERSION_MINOR(retval)
			  << " REVISION: " << PAPI_VERSION_REVISION(retval) << "\n";
}

int main(int argc, char *argv[])
{

	char c;
	int lin, col, blockSize;
	int op;

	int EventSet = PAPI_NULL;
	long long values[60];
	int ret;

	ret = PAPI_library_init(PAPI_VER_CURRENT);
	if (ret != PAPI_VER_CURRENT)
		std::cout << "FAIL" << endl;

	ret = PAPI_create_eventset(&EventSet);
	if (ret != PAPI_OK)
		cout << "ERROR: create eventset" << endl;


	ret = PAPI_add_event(EventSet, PAPI_L1_ICM);
	ret = PAPI_add_event(EventSet, PAPI_L2_DCM);
	ret = PAPI_add_event(EventSet, PAPI_L1_DCM);
	ret = PAPI_add_event(EventSet, PAPI_L2_ICM);
	ret = PAPI_add_event(EventSet, PAPI_L1_TCM);
	ret = PAPI_add_event(EventSet, PAPI_L2_TCM);
	ret = PAPI_add_event(EventSet, PAPI_L3_TCM);
	ret = PAPI_add_event(EventSet, PAPI_CA_SNP);
	ret = PAPI_add_event(EventSet, PAPI_CA_SHR);
	ret = PAPI_add_event(EventSet, PAPI_CA_CLN);
	ret = PAPI_add_event(EventSet, PAPI_CA_ITV);
	ret = PAPI_add_event(EventSet, PAPI_L3_LDM);
	ret = PAPI_add_event(EventSet, PAPI_TLB_DM);
	ret = PAPI_add_event(EventSet, PAPI_TLB_IM);
	ret = PAPI_add_event(EventSet, PAPI_L1_LDM);
	ret = PAPI_add_event(EventSet, PAPI_L1_STM);
	ret = PAPI_add_event(EventSet, PAPI_L2_LDM);
	ret = PAPI_add_event(EventSet, PAPI_L2_STM);
	ret = PAPI_add_event(EventSet, PAPI_PRF_DM);
	ret = PAPI_add_event(EventSet, PAPI_MEM_WCY);
	ret = PAPI_add_event(EventSet, PAPI_STL_ICY);
	ret = PAPI_add_event(EventSet, PAPI_FUL_ICY);
	ret = PAPI_add_event(EventSet, PAPI_STL_CCY);
	ret = PAPI_add_event(EventSet, PAPI_FUL_CCY);
	ret = PAPI_add_event(EventSet, PAPI_BR_UCN);
	ret = PAPI_add_event(EventSet, PAPI_BR_CN);
	ret = PAPI_add_event(EventSet, PAPI_BR_TKN);
	ret = PAPI_add_event(EventSet, PAPI_BR_NTK);
	ret = PAPI_add_event(EventSet, PAPI_BR_MSP);
	ret = PAPI_add_event(EventSet, PAPI_BR_PRC);
	ret = PAPI_add_event(EventSet, PAPI_TOT_INS);
	ret = PAPI_add_event(EventSet, PAPI_LD_INS);
	ret = PAPI_add_event(EventSet, PAPI_SR_INS);
	ret = PAPI_add_event(EventSet, PAPI_BR_INS);
	ret = PAPI_add_event(EventSet, PAPI_RES_STL);
	ret = PAPI_add_event(EventSet, PAPI_TOT_CYC);
	ret = PAPI_add_event(EventSet, PAPI_LST_INS);
	ret = PAPI_add_event(EventSet, PAPI_L2_DCA);
	ret = PAPI_add_event(EventSet, PAPI_L3_DCA);
	ret = PAPI_add_event(EventSet, PAPI_L2_DCR);
	ret = PAPI_add_event(EventSet, PAPI_L3_DCR);
	ret = PAPI_add_event(EventSet, PAPI_L2_DCW);
	ret = PAPI_add_event(EventSet, PAPI_L3_DCW);
	ret = PAPI_add_event(EventSet, PAPI_L2_ICH);
	ret = PAPI_add_event(EventSet, PAPI_L2_ICA);
	ret = PAPI_add_event(EventSet, PAPI_L3_ICA);
	ret = PAPI_add_event(EventSet, PAPI_L2_ICR);
	ret = PAPI_add_event(EventSet, PAPI_L3_ICR);
	ret = PAPI_add_event(EventSet, PAPI_L2_TCA);
	ret = PAPI_add_event(EventSet, PAPI_L3_TCA);
	ret = PAPI_add_event(EventSet, PAPI_L2_TCR);
	ret = PAPI_add_event(EventSet, PAPI_L3_TCR);
	ret = PAPI_add_event(EventSet, PAPI_L2_TCW);
	ret = PAPI_add_event(EventSet, PAPI_L3_TCW);
	ret = PAPI_add_event(EventSet, PAPI_SP_OPS);
	ret = PAPI_add_event(EventSet, PAPI_DP_OPS);
	ret = PAPI_add_event(EventSet, PAPI_VEC_SP);
	ret = PAPI_add_event(EventSet, PAPI_VEC_DP);
	ret = PAPI_add_event(EventSet, PAPI_REF_CYC);

		

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

		// Start counting
		ret = PAPI_start(EventSet);
		if (ret != PAPI_OK)
			cout << "ERROR: Start PAPI" << endl;

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

		ret = PAPI_stop(EventSet, values);
		if (ret != PAPI_OK)
			cout << "ERROR: Stop PAPI" << endl;

		printf("\n\t\t*** PAPI RESULTS ***\n");

		printf("\tPAPI_L1_ICM: %lld\n", values[0]);
		printf("\tPAPI_L2_DCM: %lld\n", values[1]);
		printf("\tPAPI_L1_DCM: %lld\n", values[2]);
		printf("\tPAPI_L2_ICM: %lld\n", values[3]);
		printf("\tPAPI_L1_TCM: %lld\n", values[4]);
		printf("\tPAPI_L2_TCM: %lld\n", values[5]);
		printf("\tPAPI_L3_TCM: %lld\n", values[6]);
		printf("\tPAPI_CA_SNP: %lld\n", values[7]);
		printf("\tPAPI_CA_SHR: %lld\n", values[8]);
		printf("\tPAPI_CA_CLN: %lld\n", values[9]);
		printf("\tPAPI_CA_ITV: %lld\n", values[10]);
		printf("\tPAPI_L3_LDM: %lld\n", values[11]);
		printf("\tPAPI_TLB_DM: %lld\n", values[12]);
		printf("\tPAPI_TLB_IM: %lld\n", values[13]);
		printf("\tPAPI_L1_LDM: %lld\n", values[14]);
		printf("\tPAPI_L1_STM: %lld\n", values[15]);
		printf("\tPAPI_L2_LDM: %lld\n", values[16]);
		printf("\tPAPI_L2_STM: %lld\n", values[17]);
		printf("\tPAPI_PRF_DM: %lld\n", values[18]);
		printf("\tPAPI_MEM_WCY: %lld\n", values[19]);
		printf("\tPAPI_STL_ICY: %lld\n", values[20]);
		printf("\tPAPI_FUL_ICY: %lld\n", values[21]);
		printf("\tPAPI_STL_CCY: %lld\n", values[22]);
		printf("\tPAPI_FUL_CCY: %lld\n", values[23]);
		printf("\tPAPI_BR_UCN: %lld\n", values[24]);
		printf("\tPAPI_BR_CN: %lld\n", values[25]);
		printf("\tPAPI_BR_TKN: %lld\n", values[26]);
		printf("\tPAPI_BR_NTK: %lld\n", values[27]);
		printf("\tPAPI_BR_MSP: %lld\n", values[28]);
		printf("\tPAPI_BR_PRC: %lld\n", values[29]);
		printf("\tPAPI_TOT_INS: %lld\n", values[30]);
		printf("\tPAPI_LD_INS: %lld\n", values[31]);
		printf("\tPAPI_SR_INS: %lld\n", values[32]);
		printf("\tPAPI_BR_INS: %lld\n", values[33]);
		printf("\tPAPI_RES_STL: %lld\n", values[34]);
		printf("\tPAPI_TOT_CYC: %lld\n", values[35]);
		printf("\tPAPI_LST_INS: %lld\n", values[36]);
		printf("\tPAPI_L2_DCA: %lld\n", values[37]);
		printf("\tPAPI_L3_DCA: %lld\n", values[38]);
		printf("\tPAPI_L2_DCR: %lld\n", values[39]);
		printf("\tPAPI_L3_DCR: %lld\n", values[40]);
		printf("\tPAPI_L2_DCW: %lld\n", values[41]);
		printf("\tPAPI_L3_DCW: %lld\n", values[42]);
		printf("\tPAPI_L2_ICH: %lld\n", values[43]);
		printf("\tPAPI_L2_ICA: %lld\n", values[44]);
		printf("\tPAPI_L3_ICA: %lld\n", values[45]);
		printf("\tPAPI_L2_ICR: %lld\n", values[46]);
		printf("\tPAPI_L3_ICR: %lld\n", values[47]);
		printf("\tPAPI_L2_TCA: %lld\n", values[48]);
		printf("\tPAPI_L3_TCA: %lld\n", values[49]);
		printf("\tPAPI_L2_TCR: %lld\n", values[50]);
		printf("\tPAPI_L3_TCR: %lld\n", values[51]);
		printf("\tPAPI_L2_TCW: %lld\n", values[52]);
		printf("\tPAPI_L3_TCW: %lld\n", values[53]);
		printf("\tPAPI_SP_OPS: %lld\n", values[54]);
		printf("\tPAPI_DP_OPS: %lld\n", values[55]);
		printf("\tPAPI_VEC_SP: %lld\n", values[56]);
		printf("\tPAPI_VEC_DP: %lld\n", values[57]);
		printf("\tPAPI_REF_CYC: %lld\n", values[58]);


		ret = PAPI_reset(EventSet);
		if (ret != PAPI_OK)
			std::cout << "FAIL reset" << endl;

	} while (op != 0);

	ret = PAPI_remove_event(EventSet, PAPI_L1_ICM);
	if (ret != PAPI_OK)
		std::cout << "FAIL remove event" << endl;

	ret = PAPI_remove_event(EventSet, PAPI_L2_DCM);
	if (ret != PAPI_OK)
		std::cout << "FAIL remove event" << endl;

	ret = PAPI_remove_event(EventSet, PAPI_L1_ICR);
	if (ret != PAPI_OK)
		std::cout << "ERROR: PAPI_L1_ICR" << endl;

	ret = PAPI_remove_event(EventSet, PAPI_L2_DCR);
	if (ret != PAPI_OK)
		std::cout << "ERROR: PAPI_L2_DCR" << endl;

	ret = PAPI_remove_event(EventSet, PAPI_L2_TCM);
	if (ret != PAPI_OK)
		std::cout << "ERROR: PAPI_L2_TCM" << endl;

	ret = PAPI_remove_event(EventSet, PAPI_TLB_DM);
	if (ret != PAPI_OK)
		std::cout << "ERROR: PAPI_TLB_DM" << endl;

	ret = PAPI_remove_event(EventSet, PAPI_FUL_ICY);
	if (ret != PAPI_OK)
		std::cout << "ERROR: PAPI_FUL_ICY" << endl;

	ret = PAPI_destroy_eventset(&EventSet);
	if (ret != PAPI_OK)
		std::cout << "FAIL destroy" << endl;
}
