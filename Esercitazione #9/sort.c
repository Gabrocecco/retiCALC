#include "sort.h"

void swap(classifica *a, int i, int j) {
    int temp;
    char tmps[255];
    temp = a->punti[i];
    strcpy(tmps, a->giudice[i]);
    a->punti[i] = a->punti[j];
    strcpy(a->giudice[i], a->giudice[j]);
    a->punti[j] = temp;
    strcpy(a->giudice[j], tmps);
}

void quickSortR(classifica *a, int start, int end) {
	int i, j, iPivot, pivot;
	if (start < end) {
		i = start;
		j = end;
		iPivot = end;
		pivot = a->punti[iPivot];
		do {
			while (i < j && a->punti[i] >= pivot) {  
				i++;
			}
			while (j > i && a->punti[j] <= pivot) {
				j--;
            }
			if (i < j) {
				swap(a, i, j);
			}
		} while (i < j);
		if (i != iPivot && a->punti[i] != a->punti[iPivot]) {
			swap(a, i, iPivot);
			iPivot = i;
		}
		if (start < iPivot - 1) {
			quickSortR(a, start, iPivot - 1);
		}
		if (iPivot + 1 < end) {
			quickSortR(a, iPivot + 1, end);
		}
	}
}

void bubbleSort(classifica *v, int n) {
	int i = 0;
	int ord = 0;
	while (n > 1 && !ord) {
		ord = 1;
		for (i = 0; i < n - 1; i++) {
			if (v->punti[i] < v->punti[i + 1]) {
				swap(v, i, i+1);
				ord = 0;
			}
		}
		n--;
	}
}