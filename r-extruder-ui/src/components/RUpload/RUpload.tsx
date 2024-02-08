import { Loader } from "@progress/kendo-react-indicators";
import React, { useState } from "react";
import { Field, Form } from "@progress/kendo-react-form";
import { Checkbox } from "@progress/kendo-react-inputs";
import DropDownListInput from "../SingleSelect";
import "./RUpload.scss";
import UploadInput from "../UploadInput/UploadInput";
import S3KeyDisplay from "../S3Display/S3Display";
import { FileWithRaw } from "../../utils/types";
import SuccessNotification from "../SuccessNotification";

const RUpload = () => {
  const [s3Key, setS3Key] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [checkedName, setCheckedName] = useState<boolean>(false);
  const [successConversion, setSuccessConversion] = useState<boolean>(false);

  const handleInsertFile = (e: boolean) => {
    setCheckedName(e);
  };
  const handleFileDownload = async (
    response: Response,
    files: FileWithRaw[]
  ) => {
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const word = document.createElement("a");
    word.href = url;
    word.download = files[0].getRawFile().name.replace(".R", ".docx");
    document.body.appendChild(word);
    word.click();
    word.remove();
  };

  const handleSubmit = async (dataItem: {
    // This is temporary solution, since this will not be used if project becomes official
    // eslint-disable-next-line
    [name: string]: any;
  }) => {
    setLoading(true);
    const { files, ...otherFields } = dataItem;

    const formData = new FormData();

    files.forEach((file: FileWithRaw) => {
      formData.append("files", file.getRawFile());
    });

    Object.keys(otherFields).forEach((prop) => {
      formData.append(prop, otherFields[prop]);
    });

    try {
      const response = await fetch("http://localhost:8080/api/extractors", {
        method: "POST",
        body: formData,
      });
      if (dataItem.output === "docx") {
        await handleFileDownload(response, files);
        setSuccessConversion(true);
      } else {
        const data = await response.json();
        setS3Key(data["s3 key"]);
        setSuccessConversion(true);
      }
    } catch (error) {
      console.error("Error:", error);
    } finally {
      setLoading(false);
    }
  };
  const copyToClipboard = () => {
    if (s3Key) {
      navigator.clipboard
        .writeText(s3Key)
        .then(() => {})
        .catch((error) => {
          console.error("Error copying S3 key to clipboard:", error);
        });
    }
  };
  const handleCloseNotification = () => {
    setSuccessConversion(false);
  };
  return (
    <div className="loader">
      {loading && (
        <div className="loader-overlay">
          <Loader type="converging-spinner" />
        </div>
      )}
      <SuccessNotification
        successConversion={successConversion}
        onClose={handleCloseNotification}
      />
      <Form
        onSubmit={handleSubmit}
        render={(formRenderProps) => (
          <form onSubmit={formRenderProps.onSubmit} className={"k-form"}>
            <fieldset>
              <legend>Upload your R files:</legend>
              <div className="upload-input">
                <Field
                  name={"files"}
                  component={UploadInput}
                  setCheckedName={setCheckedName}
                  handleFileChange={handleInsertFile}
                />
              </div>
              <div className="dropdown-list">
                <Field
                  name={"output"}
                  component={DropDownListInput}
                  label={"Select your returning parameter"}
                />
              </div>
              <div className="checkbox">
                <Field
                  name={"pdf"}
                  component={Checkbox}
                  label={
                    "Do you want whole plot movement instead of one picture?"
                  }
                />
              </div>
              <S3KeyDisplay s3Key={s3Key} copyToClipboard={copyToClipboard} />
              <div className="submit-button">
                {checkedName && <p> Only .R files are allowed.</p>}
                <button
                  type={"submit"}
                  className="k-button"
                  disabled={!formRenderProps.allowSubmit || checkedName}
                >
                  Submit
                </button>
              </div>
            </fieldset>
          </form>
        )}
      />
    </div>
  );
};

export default RUpload;
