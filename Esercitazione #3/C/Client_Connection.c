#include <stdio.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <string.h>
#include <netdb.h>
#include <fcntl.h>
#include <unistd.h>

#define BUFFER_SIZE 262144

int main(void){
   struct hostent *host;
	struct sockaddr_in clientaddr, servaddr;
	int  port, sd, ris, len, fd, riga;
	char c;
    char nomeFile[256];
    char buffer[BUFFER_SIZE];

	memset((char *)&clientaddr, 0, sizeof(struct sockaddr_in));
	clientaddr.sin_family = AF_INET;
	clientaddr.sin_addr.s_addr = INADDR_ANY;

	clientaddr.sin_port = 0;

	memset((char *)&servaddr, 0, sizeof(struct sockaddr_in));
	servaddr.sin_family = AF_INET;
	host = gethostbyname ("localhost");

    servaddr.sin_addr.s_addr=((struct in_addr *)(host->h_addr))->s_addr;
	servaddr.sin_port = htons(8000);

    sd = socket(AF_INET, SOCK_STREAM, 0);
    if (sd < 0) {
        perror("Errore nella creazione della socket");
        exit(0);
    }

    if (connect(sd,(struct sockaddr *) &servaddr, sizeof(struct sockaddr)) < 0) {
        perror("Errore nella connessione");
        exit(1);
    }

    scanf("%s", nomeFile);
    
    if (access(nomeFile, /*R_OK | W_OK*/ F_OK)) {
        printf("Errore il file non esiste o non può essere letto/scritto\n");
        exit(2);
    }
    
    riga = 0;

    scanf("%d", &riga);

    if (riga <= 0) {
        printf("Inserire un numero maggiore di zero\n");
        exit(3);
    }

    if ((fd = open(nomeFile, O_RDONLY)) < 0) {
        printf("Errore nell'apertura del file %d\n", fd);
        exit(4);
    }
    
    write(sd, &riga, sizeof(int));

    while ((len = read(fd, buffer, BUFFER_SIZE)) > 0) {
        write(sd, buffer, len);
    }
    
    shutdown(sd, SHUT_WR);
    close(fd);
    
    unlink(nomeFile);

    fd = open(nomeFile, O_WRONLY | O_CREAT);
    
    while ((len = read(sd, buffer, BUFFER_SIZE)) > 0) {
        write(fd, buffer, len);
        printf("IN\n");
    }
    
    shutdown(sd, SHUT_RD);
    close(sd);
    close(fd);

    return 0;
}
