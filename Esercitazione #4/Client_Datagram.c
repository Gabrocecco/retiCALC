#include <stdio.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <string.h>
#include <netdb.h>

int main() {

    struct hostent *host;
	struct sockaddr_in clientaddr, servaddr;
	int  port, sd, len;
	char c;

    memset((char *)&clientaddr, 0, sizeof(struct sockaddr_in));
	clientaddr.sin_family = AF_INET;
	clientaddr.sin_addr.s_addr = INADDR_ANY;

    clientaddr.sin_port = 0;

	memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
	servaddr.sin_family = AF_INET;
	host = gethostbyname ("localhost");

    servaddr.sin_addr.s_addr=((struct in_addr *)(host->h_addr))->s_addr;
	servaddr.sin_port = htons(8001);

    sd=socket(AF_INET, SOCK_DGRAM, 0);  //creo una socket 
	if (sd < 0) {
        perror("Erroe durante l'apertura della socket");
        exit(1);
    }

    if (bind(sd,(struct sockaddr *) &clientaddr, sizeof(clientaddr)) < 0) { //daccio il bind della socket
        perror("Errore durante il binding della socket");
        exit(1);
    }

    char request[256];
    char line[256];
    int response;

    for (;;) {
        printf("Inserire nome di file\n");
        scanf("%s", request);
        printf("Inserire la parola da cancellare\n");
        scanf("%s", line);

        if (sendto(sd, request, sizeof(request), 0, (struct sockaddr*)&servaddr, len) < 0) {    //invio nome del file
            perror("Errore nella send");
            exit(2);
        } 
        if (sendto(sd, line, sizeof(line), 0, (struct sockaddr*)&servaddr, len) < 0) {  //invio parola da cancellare
            perror("Errore nella send");
            exit(2);
        } 

        if (recvfrom(sd, response, sizeof(response), 0, (struct sockaddr *)&servaddr, &len) < 0) {  //leggo la risposta (numero parole cancellate)
            perror("Errore nella receive");
            exit(3);
        }

        if (response < 0) {
            printf("Errore\n");
        } else {
            printf("Parole cancellate: %d\n", response);    //stampo numero di parola cancellate
        }

    }

   

}