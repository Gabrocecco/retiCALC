#include <stdio.h>
#include <rpc/rpc.h>
#include <unistd.h>
#include <fcntl.h>
#include <dirent.h>
#include <stdlib.h>
#include "scan.h"

#define BUFFER_SIZE 8196

#define FILE_NOT_FOUND -1
#define CANT_OPEN_FILE -2

response *file_scan_1_svc(String *filename, struct svc_req *rp) {
    static response *res;

    xdr_free((xdrproc_t) xdr_response, res);
    res = (response*) malloc(sizeof(res));

    int fd = open(*filename, O_RDONLY);
    if (fd < 0) {
        res->chars = FILE_NOT_FOUND;
        return res;
    }
    
    char buffer[BUFFER_SIZE];
    int nread, pos;
    char lastchar = ' ';
    int chars = 0;
    int words = 0;
    int rows = 1;

    while ((nread = read(fd, buffer, BUFFER_SIZE)) > 0) {
        pos = 0;
        chars += nread;
        while (pos < nread) {
            if (buffer[pos] == ' '  || buffer[pos] == '\n') {
                if (pos == 0 && lastchar != ' ' && lastchar != '\n') {
                    words++;
                } else if (buffer[pos - 1] != ' ' && buffer[pos - 1] != '\n') {
                    words++;
                }
                if (buffer[pos] == '\n') {
                    rows++;
                }
            }
            pos++;
        }
        lastchar = buffer[pos - 1];
    }

    if (lastchar != ' ' && lastchar != '\n') {
        words++;
    }

    res->chars = chars;
    res->words = words;
    res->rows = rows;

    return res;
}

int *dir_scan_1_svc(params *p, struct svc_req *rp) {
    static int res;

    int len, fd;

    char* dir = p->dir;
    int min = p->min;

    DIR* d = opendir(dir);
    struct dirent *element;
    
    if (d == NULL) {
        res = FILE_NOT_FOUND;
        return &res;
    }

    while (element = readdir(d)) {
        if (element->d_type == DT_REG) {
            fd = open(element->d_name, O_RDONLY);
            if (fd < 0) {
                res = CANT_OPEN_FILE;
                return &res;
            }
            len = lseek(fd, 0, SEEK_END);
            close(fd);
            if (len > min) {
                res++;
            }
        }
    }

    closedir(d);

    return &res;

}