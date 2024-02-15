import React, { useState } from "react";
import axios from "axios";
import "./WordDownload.scss";
import { Notification } from "@basf/react-components";
import { TNotification } from "@basf/react-components/lib/types";

const WordDownload: React.FC = () => {
  const [fileName, setFileName] = useState("");
  const [errMsg, setErrMsg] = useState("");
  const [notifications, setNotifications] = useState<TNotification[]>([]);

  const handleDownload = async () => {
    try {
      const response = await axios.get(
        `http://localhost:8080/api/extractors/s3/${fileName}`,
        { responseType: "arraybuffer" }
      );
      const blob = new Blob([response.data], {
        type: response.headers["content-type"],
      });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", fileName);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (error) {
      console.error("Error downloading file:", error);
      setNotifications([
        {
          message: `Error downloading file. ${(error as Error).message}`,
          type: "error",
        },
      ]);
    }
  };

  const handleSendData = async () => {
    if (fileName === "") {
      setErrMsg("Please fill out this field");
      return;
    }
    setErrMsg("");
    await handleDownload();
  };

  return (
    <div className="s3-download">
      <h2 id={"titleGettingS3Key"}>DOWNLOAD YOUR WORD DOCUMENT</h2>
      <input
        type="text"
        id={"inputS3Key"}
        value={fileName}
        onChange={(e) => setFileName(e.target.value)}
        placeholder="Enter S3 key"
      />
      {errMsg && <p> {errMsg}</p>}
      <button onClick={handleSendData} id={"downloadS3Key"}>
        Download
      </button>
      <Notification
        notifications={notifications}
        setNotifications={() => null}
      />
    </div>
  );
};

export default WordDownload;
