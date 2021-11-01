#include <stdio.h>
#include <stdlib.h>
#include <sys/wait.h>
#include <unistd.h>
#include <fcntl.h>

int main(void) {
    int status, flol, written, random;
    int fd = open("/root/Desktop/results.txt", O_WRONLY);
    char spazio = ' ';
    char a = 'a';
    if (fd < 0) {
        perror("ERRORE\n");
        exit(0);
    }
    close(1);
    dup(fd);
    close(fd);
    for (int k = 1; k < 101; k++) {
        unlink("/root/Desktop/lol.txt");
        flol = open("/root/Desktop/lol.txt", O_CREAT|O_WRONLY|O_TRUNC);
        if (flol < 0) {
            exit(2);
        }
        written = 0;
        while (written < 1048576) {
            write(flol, &spazio, 1);
            random = rand() % (2*(k - 1) + 1) + 1;
            written += random + 1;
            for (int r = 0; r < random; r++) {
                write(flol, &a, 1);
            }
        }
        close(flol);
        for (int i = 0; i < 100; i++) {
            if (!fork()) {
                execl("/root/Desktop/out/Test.c.sh","/root/Desktop/out/Test.c.sh","/root/Desktop/lol.txt", "8192", 0);
                printf("ERRORE\n");
                exit(0);
            }
            wait(&status);
        }
        for (int i = 0; i < 100; i++) {
            if (!fork()) {
                execl("/root/Desktop/out/Test2.c.sh","/root/Desktop/out/Test2.c.sh","/root/Desktop/lol.txt", "8192", 0);
                printf("ERRORE\n");
                exit(0);
            }
            wait(&status);
        }
    }
}
