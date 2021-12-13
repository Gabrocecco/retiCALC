#include <stdio.h>
#include <rpc/rpc.h>

#include "scan.h"

int main(int argc, char** argv) {
    CLIENT *cl;
    char *host;
    params *p;
    response *r;
    int *res;
    char c;
    int min;
    char **nomefile;

    if (argc < 2) {
        printf("Inserire il nome dell'host\n");
        exit(0);
    }

    host = argv[1];

    cl = clnt_create(host, SCANPROG, SCANVERS, "udp");
    if (cl == NULL) {
		clnt_pcreateerror(host);
		exit(1);
	}

    p = (params *) malloc(sizeof(params));
    nomefile = (char **) malloc(sizeof(char) * 255);

    printf("Inserisci il servizio (F per file_scan D per dir_scan)\n");
    while (scanf("%c", &c) != EOF) {
        switch (c) {
        case 'F' :
            printf("Inserisci il nome del file:\n");
            scanf("%s", *nomefile);
            gets();
            r = file_scan_1(nomefile, cl);
            
            if (r == NULL) {
                printf("Chiamata fallita\n");
                continue;
            }
            if (r->chars == -1) {
                printf("Il file inserito non esiste\n");
                continue;
            }
            if (r->chars == 0) {
                printf("File vuoto\n");
                continue;
            }
            printf("Caratteri: %d Parole: %d Righe:%d\n", r->chars, r->words, r->rows);
            break;
        
        case 'D' :
            printf("Inserisci il nome della directiory:\n");
            scanf("%s", *nomefile); 
            gets();
            printf("Inserisci il numero minimo di caratteri:\n");
            scanf("%d", &min);
            gets();
            p->dir = *nomefile;
            p->min = min;
            res = dir_scan_1(p, cl);
            if (*res == -1) {
                printf("La directory non esiste\n");
                continue;
            }
            if (*res == -2) {
                printf("Errore nella lettura di uno dei file\n");
                continue;

            }
            printf("Nel direttorio sono presenti %d file\n", *res);
            break;

        default:
            printf("Inserisci un carattere valido\n");
            break;
        }
    }

    free(p);
    clnt_destroy(cl);

}