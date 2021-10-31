#include <stdio.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <string.h>
#include <netdb.h>

int main(){
   struct hostent *host;
	struct sockaddr_in clientaddr, servaddr;
	int  port, sd, ris, len;
	char c;

	memset((char *)&clientaddr, 0, sizeof(struct sockaddr_in));
	clientaddr.sin_family = AF_INET;
	clientaddr.sin_addr.s_addr = INADDR_ANY;

	clientaddr.sin_port = 0;

	memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
	servaddr.sin_family = AF_INET;
	host = gethostbyname ("localhost");

    servaddr.sin_addr.s_addr=((struct in_addr *)(host->h_addr))->s_addr;
	servaddr.sin_port = htons(8000);

    sd=socket(AF_INET, SOCK_DGRAM, 0);
	if (sd<0) {
        perror("Erroe durante l'apertura della socket");
        exit(1);
    }

    if (bind(sd,(struct sockaddr *) &clientaddr, sizeof(clientaddr))<0) {
        perror("bind socket "); exit(1);
    }

    printf("Inserisci nomi di file\n");

    char nomeFile[256];
    len = sizeof(servaddr);
	
    while (scanf("%s", nomeFile) != EOF) {
        if (sendto(sd, nomeFile, sizeof(nomeFile), 0, (struct sockaddr*)&servaddr, len) < 0) {
            perror("Errore nella send");
            continue;
        } 
        if (recvfrom(sd, &ris, sizeof(ris), 0, (struct sockaddr *)&servaddr, &len) < 0) {
            perror("Errore nella receive");
            continue;
        }
        if (ris < 0) {
            printf("Il file non esiste\n");
        } else {
            printf("La parola di lunghezza massima ha %d caratteri\n", ris);
        }
    }
}
