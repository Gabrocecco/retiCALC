#include <stdio.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <string.h>
#include <netdb.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <signal.h>

#define BUFFER_SIZE 262144

void handler(int SIGNUM);

int main(void) {
	int sd, len, num1, num2, ris, fd, conn_sd;
	const int on = 1;
	struct sockaddr_in cliaddr, servaddr;
	struct hostent *clienthost;
    char buffer[BUFFER_SIZE];

	memset ((char *)&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = INADDR_ANY;  
	servaddr.sin_port = htons(8000);  
    
    signal(SIGCHLD, handler);

	sd=socket(AF_INET, SOCK_STREAM, 0);
	if (sd < 0) {
        perror("Errore nella creazione socket");
        exit(1);
    }

	if (setsockopt(sd, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
        perror("Errore nelle opzioni della socket");
        exit(2);
    }

	if (bind(sd,(struct sockaddr *) &servaddr, sizeof(servaddr)) < 0) {
        perror("Errore nel binding");
        exit(3);
    }

    if (listen(sd, 5) < 0) {
        perror("Errore nella listen"); 
        exit(4);
    }

    for (;;) {
        if((conn_sd = accept(sd,(struct sockaddr *)&cliaddr,&len)) < 0) {
			if (errno==EINTR) {
				perror("Forzo la continuazione della accept");
				continue;
			}
			else exit(1);
		}

        if (!fork()) {
                            printf("HERE\n");
            close(sd);
            int riga;
            if (read(conn_sd, &riga, sizeof(int)) <= 0) {
                printf("Errore\n");
                exit(0);
            }
            int numRighe = 1;
            int newBufSize = 0;
            int lineFlag = 1;
            while ((len = read(conn_sd, buffer, BUFFER_SIZE)) > 0) {
                for (int i = 0; i < len; i++) {
                    if (lineFlag && (numRighe == riga)) {
                        newBufSize = i;
                        lineFlag = 0;
                    } else if (!lineFlag && (numRighe != riga)) {
                        buffer[newBufSize] = buffer[i];
                        newBufSize++;
                    }
                    if (buffer[i] == '\n') {
                        numRighe++;
                    }
                }

                write(conn_sd, buffer, newBufSize);
            }
            
            shutdown(conn_sd, SHUT_RD);
            shutdown(conn_sd, SHUT_WR);
            close(conn_sd);
        }

        close(conn_sd);
    }
}

void handler(int SIGNUM) {
    wait();
    return;
}
