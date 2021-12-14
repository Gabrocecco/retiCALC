#include <stdio.h>
#include <rpc/rpc.h>
#include <unistd.h>
#include <fcntl.h>
#include <dirent.h>
#include <stdlib.h>
#include "votafattorex.h"
#include "struct.h"
#include "sort.h"

extern l voti[];

int init = 0;

int* aggingi_voto_1_svc(voto* voto, struct svc_req *rp) {
    static int result = 0;
    for (int i = 0; i < N; i++) {
        if (!strcmp(voto->candidato, voti[i].candidato)) {
            if (voto->operazione == 'A') {
                voti[i].voto += 1;
            } else {
                if (voti[i].voto > 0) {
                    voti[i].voto -= 1;
                }
            }
            result = 1;
            break;
        }
    }
    return &result;
}

classifica* classifica_voto_1_svc(void* v ,struct svc_req *rp) {
    static classifica * result;
    
    if (init) {
        xdr_free((xdrproc_t) xdr_classifica, (char *) result);
    }
    result = (classifica *) malloc(sizeof(classifica));

    for (int i = 0; i < NUMGIUDICI; i++) {
        result->giudice[i] = (char *) malloc(sizeof(char) * 255);
        strcpy(result->giudice[i], "");
        result->punti[i] = 0;
    }
    int last = 0;
    int found = 0;
    for (int i = 0; i < N; i++) {
        if (voti[i].voto != -1) {
            found = 0;
            for (int k = 0; k < NUMGIUDICI; k++) {
                if (!strcmp(result->giudice[k], voti[i].giudice)) {
                    result->punti[k] += voti[i].voto;
                    found = 1;
                    break;
                }
            }
            if (!found) {
                strcpy(result->giudice[last], voti[i].giudice);
                result->punti[last] += voti[i].voto;
                last++;
            }
        }
    }
    quickSortR(result, 0, NUMGIUDICI - 1);
    init = 1;
    return result;
}
