#include <stdio.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <string.h>
#include <netdb.h>
#include <fcntl.h>
#include <unistd.h>

#define BUFFER_SIZE 256

int main(void) {
    
    struct hostent *host;
	struct sockaddr_in clientaddr, servaddr;
	int  port, sd;

    memset((char *)&clientaddr, 0, sizeof(struct sockaddr_in));
	clientaddr.sin_family = AF_INET;
	clientaddr.sin_addr.s_addr = INADDR_ANY;

	clientaddr.sin_port = 0;

	memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
	servaddr.sin_family = AF_INET;
	host = gethostbyname ("localhost");

    servaddr.sin_addr.s_addr=((struct in_addr *)(host->h_addr))->s_addr;
	servaddr.sin_port = htons(8000);    //imposto il server addr sulla porta 8000

    sd = socket(AF_INET, SOCK_STREAM, 0);   //creo la socket
    if (sd < 0) {
        perror("Errore nella creazione della socket");
        exit(0);
    }

    if (connect(sd,(struct sockaddr *) &servaddr, sizeof(struct sockaddr)) < 0) {   //istauro una connessione con il server
        perror("Errore nella connessione");
        exit(1);
    }

    char request[BUFFER_SIZE];
    char response[BUFFER_SIZE];

    for (;;) {
        printf("Inserire il nome di una directory\n");
        scanf("%s", request);

        write(sd, request, sizeof(request));    //iterativamente invio un nome di una directory

        while (read(sd, response, BUFFER_SIZE) > 0) {   //iterativamente legge e stampa la risposta del server.
            printf("%s\n", response);
        }
    }

}