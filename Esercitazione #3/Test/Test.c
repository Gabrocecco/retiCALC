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

int main(int argc, char** argv) {
    
    int maxLen = 0;
    int cur = 0;
    char curChar;
    int curRead;
    int curPos;
    int bufSize;
    int i;
    
    struct timeval t1;
    struct timeval t2;
    gettimeofday(&t1,NULL);
    
    int BUFFER_SIZE = atoi(argv[2]);
    
    char buffer[BUFFER_SIZE];
    
    int fd = open(argv[1], O_RDONLY);
    if (fd < 0) {
        printf("Errore\n");
        exit(0);
    }
    
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
                if (buffer[i - 1] == '\n' || buffer[i - 1] == ' ') {
                    cur = 0;
                }
            }
        }
    }
    gettimeofday(&t2, NULL);
    
   // printf("%d\n", maxLen);
    
    printf("%d\n" ,1000000 * t2.tv_sec + t2.tv_usec - 1000000 * t1.tv_sec - t1.tv_usec);
    
    close(fd);
    
    exit(/*1000000 * t2.tv_sec + t2.tv_usec - 1000000 * t1.tv_sec - t1.tv_usec*/ 0);
    
    return 0;
}
