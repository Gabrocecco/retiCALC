#include <stdio.h>

#include "votafattorex.h"

int main(int argc, char** argv) {
    CLIENT *cl;
    char *host;
    int *res;
    char c;
    voto *v;
    classifica *r;
    char nomeCandidato[255];
    char operazione;


    if (argc < 2) {
        printf("Inserire il nome dell'host\n");
        exit(0);
    }

    host = argv[1];

    cl = clnt_create(host, VOTAFATTOREX, VOTOVERS, "udp");
    if (cl == NULL) {
		clnt_pcreateerror(host);
		exit(1);
	}

    v = (voto *) malloc(sizeof(voto));
    v->candidato = (char *) malloc(sizeof(char) * 255);

    printf("Inserisci il servizio (V per aggiungi_voto C per classifica_voto)\n");
    while (scanf("%c", &c) != EOF) {
        gets();
        switch (c) {
        case 'V' :
            printf("Inserisci il nome del candidato:\n");
            scanf("%s", nomeCandidato);
            gets();
            printf("Inserisci l'operazione (A per aggiungere S per sottrarre):\n");
            scanf("%c", &operazione);
            gets();
            while (operazione != 'A' && operazione != 'S') {
                printf("Inserire solo A o S\n");
                scanf("%c", &operazione);
                gets();
            }
            strcpy(v->candidato, nomeCandidato);
            v->operazione = operazione;
            res = aggingi_voto_1(v, cl);
            if (res == NULL) {
                printf("Chiamata fallita\n");
                continue;
            }
            if (!*res) {
                printf("Il candidato non esiste\n");
                continue;
            }
            printf("Voto inserito con successo\n");
            free(v->candidato);
            free(v);
            break;
        
        case 'C' :
            r = classifica_voto_1(0, cl);

            if (r == NULL) {
                printf("Chiamata fallita\n");
                continue;
            }
            
            printf("La classifica è la seguente:\n");
            for (int i = 0; i < NUMGIUDICI; i++) {
                printf("%s: %d punti\n", r->giudice[i], r->punti[i]);
            }
            break;

        default:
            printf("Inserisci un carattere valido\n");
            break;
        }
    }

    clnt_destroy(cl);

}