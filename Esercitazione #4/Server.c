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
#include <dirent.h>

#define BUFFER_SIZE 256
#define BLOCK_SIZE 8196

void handler(int SIGNUM);

int main(void) {

	int sd_connection, len, conn_sd, sd_datagram, fd;
	const int on = 1;
	struct sockaddr_in cliaddr, servaddr;
	struct hostent *clienthost;

    memset ((char *)&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_addr.s_addr = INADDR_ANY;  
	servaddr.sin_port = htons(8000);  
    
    signal(SIGCHLD, handler);

	sd_connection = socket(AF_INET, SOCK_STREAM, 0);
	if (sd_connection < 0) {
        perror("Errore nella creazione socket con connessione");
        exit(1);
    }

    sd_datagram = socket(AF_INET, SOCK_DGRAM, 0);
    if (sd_datagram < 0) {
        perror("Errore nella creazione socket senza connessione");
        exit(1);
    }

    if (setsockopt(sd_connection, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
        perror("Errore nelle opzioni della socket con connessione");
        exit(2);
    }

    if (setsockopt(sd_datagram, SOL_SOCKET, SO_REUSEADDR, &on, sizeof(on)) < 0) {
        perror("Errore nelle opzioni della socket senza connessione");
        exit(2);
    }

	if (bind(sd_connection,(struct sockaddr *) &servaddr, sizeof(servaddr)) < 0) {
        perror("Errore nel binding della socket con connessione");
        exit(3);
    }

    servaddr.sin_port = htons(8001);
    if (bind(sd,(struct sockaddr *) &servaddr, sizeof(servaddr)) < 0) {
        perror("Errore nel binding della socket senza connessione");
        exit(3);
    }

    if (listen(sd_connection, 5) < 0) {
        perror("Errore nella listen"); 
        exit(4);
    }

    char request[BUFFER_SIZE];
    int response;

    int rd_mask = 0;
    int res;

    DIR* directory;
    DIR* sottoDir;
    struct dirent* element;
    struct dirent* sottoEl;

    char line[BUFFER_SIZE];
    char block[BLOCK_SIZE];
    int nread;
    int pos;
    int inPos;
    int stato;
    int wLen;
    int occ;
    int newFd;
    int mark;

    for (;;) {
        FD_ZERO(&rd_mask); 
        FD_SET(3,&rd_mask);
        FD_SET(4,&rd_mask);
        res = select(5, &rd_mask, NULL, NULL, (struct timeval *) 0);
        if (res == -1) {
            perror("Errore nella select");
            exit(5);
            if (FD_ISSET(3, &rd_mask)) {

                if ((conn_sd = accept(sd, (struct sockaddr *)&cliaddr, &len)) < 0) {
			        if (errno==EINTR) {
				        perror("Forzo la continuazione della accept");
				        continue;
			        } else {
                        exit(6);
                    }
		        }

                if (!fork()) {
                    
                    close(sd);
                    if (read(conn_sd, request, sizeof(request)) <= 0) {
                        printf("Errore\n");
                        exit(1);
                    }

                    shutdown(conn_sd, SHUT_RD);

                    if ((directory = opendir(request) == NULL) {
                        write(conn_sd, "La directory non esiste");
                    } else {
                        while ((element = readdir(directory)) != NULL) {
                            if (strcmp(element->d_name, ".") && strcmp(element->d_name, "..") && (sottoDir = opendir(element->d_name) != NULL)) {
                                while ((sottoEl = readdir(sottoDir)) != NULL) {
                                    write(conn_sd, sottoEL->d_name, strlen(sottoEl->d_name));
                                }
                            }
                        }
                    }

                    shutdown(conn_sd, SHUT_WR);
                    close(conn_sd);
                    exit(0);

                }
                close(conn_sd);
            }
            if (FD_ISSET(4, &rd_mask)) {

                if (recvfrom(sd_datagram, request, sizeof(request), 0, (struct sockaddr *)&cliaddr, &len) < 0) {
                    perror("Errore nella receive");
                    exit(1);
                }

                if (!fork()) {

                    if ((fd = open(request, O_RDWR)) < 0) {
                        response = -1;
                        sendto(sd_datagram, response, sizeof(response), 0, (struct sockaddr *)&cliaddr, len);
                    } else {
                        if ((newFd = open(strcat(request, "temp"), O_CREAT | O_RDWR)) < 0) {
                            response = -1;
                            sendto(sd_datagram, response, sizeof(response), 0, (struct sockaddr *)&cliaddr, len);
                            perror("Errore");
                            exit(0);
                        }
                         if (recvfrom(sd_datagram, line, sizeof(line), 0, (struct sockaddr *)&cliaddr, &len) < 0) {
                            perror("Errore nella receive");
                            exit(1);
                        }   
                        wLen = strlen(request);
                        occ = 0;
                        while (nread = read(fd, block, BLOCK_SIZE) > 0) {
                            pos = 0;
                            mark = 0;
                            while (pos < nread) {
                                if (block[pos] != ' ' && block[pos] != '\n') {
                                    inPos = 0;
                                    while (inPos < wLen && line[inPos] == block[pos] && pos < nread) {
                                        inPos++;
                                        pos++;
                                    }
                                    if (inPos == wLen) {
                                        occ++;
                                        write(newFd, block + mark, pos - inPos - mark);
                                        mark = pos;
                                    }
                                } else {
                                    pos++;
                                }
                            }
                            write(newFd, block + mark, nread - mark);
                        }
                        if (sendto(sd, occ, sizeof(occ), 0, (struct sockaddr *)&cliaddr, len) < 0) {
                             perror("Errore nella send");
                        }
                        exit(1);
                    }

                }

            }
        }
    }
}

void handler(int SIGNUM) {
    wait();
    return;
}