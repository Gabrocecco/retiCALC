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
#include <time.h>

void handler(int signum);

int main(int argc, char** argv) {

    struct timeval t1;
    struct timeval t2;


    signal(SIGUSR1, handler);

    /*if (!fork()) {
        sleep(5);
        kill(-222, 9);
        exit(0);
    }*/

    int fd;

    fd = open("/home/studente/Desktop/res.txt", O_RDWR | O_CREAT , 0777);

    if (fd < 0) {
        printf("UMPH\n");
        exit(0);
    }


        for (int i = 0; i < atoi(argv[1]); i++) {
            if (!fork()) {
                for(int k = 0; k < 100; k++) {
                   // setpgid(getpid(), 222);
                    if (!fork()) {
                       // setpgid(getpid(), 222);
                        execl("/home/studente/Desktop/Es4_tests/Client_Stream.out", "/home/studente/Desktop/Es4_tests/Client_Stream.out", NULL);
                    }
                    wait();
                }
                exit(0);
            }
        }

        for (int l = 0; l < atoi(argv[1]); l++) {
            wait();
        }

    close(fd);
    return 0;
}

void handler(int signum) {
    kill(-222, 9);
    exit(0);
}