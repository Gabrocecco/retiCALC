#include <stdio.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <string.h>
#include <netdb.h>
#include <unistd.h>
#include <fcntl.h>
#include <signal.h>

#define MAX_STRING_LENGTH 256
#define BUFFER_SIZE 262144

void handler(int SIGNUM);

int main(int argc, char** argv) {
	int sd, len, num1, num2, ris, fd;
	const int on = 1;
	struct sockaddr_in cliaddr, servaddr;
	struct hostent *clienthost;
    char nomeFile[MAX_STRING_LENGTH];

    if (argc != 2) {
        printf("Inserire un argomento\n");
        exit(0);
    }

    if (access(argv[1], R_OK)) {
        printf("La cartella deve esistere e deve poter essere letta\n");
        exit(0);
    }
    
    signal(SIGCHLD, handler);

	memset ((char *)&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = INADDR_ANY;  
	servaddr.sin_port = htons(8000);  

	sd=socket(AF_INET, SOCK_DGRAM, 0);
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

    len = sizeof(struct sockaddr_in);

    for (;;) {
        if (recvfrom(sd, nomeFile, sizeof(nomeFile), 0, (struct sockaddr *)&cliaddr, &len) < 0) {
            perror("Errore nella receive");
            continue;
        }
        if (!fork()) {
            if ((fd = open(strcat(argv[1], nomeFile), O_RDONLY, 0777)) < 0) {
                printf("Errore nell'apertura del file\n");
                int err = -1;
                sendto(sd, &err, sizeof(err), 0, (struct sockaddr *)&cliaddr, len);
                exit(0);
            }
            
            int maxLen = 0;
            int cur = 0;
            char buffer[BUFFER_SIZE];
            int bufSize;
            int i;
            
            while ((bufSize = read(fd, buffer, BUFFER_SIZE)) > 0) {
                i = 0;
                while (i < bufSize) {
                    if (buffer[i] == '\n' || buffer[i] == ' ') {
                        i = i + maxLen + 1;
                        cur = 0;
                    } else {
                        do {
                            i--;
                            cur++;
                        } while (i > 0 && buffer[i] != '\n' && buffer[i] != ' ');
                        i = i + cur + 1;
                        while (i < bufSize && buffer[i] != '\n' && buffer[i] != ' ') {
                            cur++;
                            i++;
                        }
                        if (cur > maxLen) {
                            maxLen = cur;
                        }
                    }
                }   
            }
            
            close(fd);

            if (sendto(sd, &maxLen, sizeof(maxLen), 0, (struct sockaddr *)&cliaddr, len)<0) {
                perror("Errore nella send");
            }
            exit(1);
        }
    }
}

void handler(int SIGNUM) {
    wait();
    return;
}
